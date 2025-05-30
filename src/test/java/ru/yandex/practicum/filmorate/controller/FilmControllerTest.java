package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.*;
import ru.yandex.practicum.filmorate.storage.impl.*;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmStorageDbImpl.class,
        FilmRowMapper.class,
        MpaStorageDbImpl.class,
        MpaRowMapper.class,
        GenreStorageDbImpl.class,
        GenreRowMapper.class,
        UserStorageDbImpl.class,
        DirectorStorageDbImpl.class,
        DirectorRowMapper.class,
        UserRowMapper.class})
public class FilmControllerTest {

    @Autowired
    private FilmStorage filmStorage;

    @Autowired
    private MpaStorage mpaStorage;

    @Autowired
    private GenreStorage genreStorage;

    @Autowired
    private DirectorStorage directorStorage;

    @Autowired
    private UserStorage userStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private FilmController filmController;
    private FilmRequest filmRequest;

    @BeforeEach
    public void setUp() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "_film", "_like", "_user");
        jdbcTemplate.execute("ALTER TABLE _film ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE _like ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE _user ALTER COLUMN id RESTART WITH 1");

        filmController = new FilmController(new FilmService(filmStorage, mpaStorage, genreStorage, directorStorage, userStorage));
        filmRequest = new FilmRequest("Test Film", "Description of Test Film",
                120, LocalDate.of(2022, 1, 1), null, null, null);
        Mpa mpa = new Mpa();
        mpa.setId(1);
        filmRequest.setMpa(mpa);
        Genre genre = new Genre();
        genre.setId(1);
        filmRequest.setGenres(List.of(genre));
        Director director = new Director();
        director.setId(1);
        director.setName("Quentin Tarantino");
        directorStorage.createDirector(director);
        filmRequest.setDirectors(List.of(director));
    }

    @Test
    public void testSearchFilmsWorksCorrectly() {
        filmController.createFilm(filmRequest);
        assertEquals(1, filmController.findAll().size(), "Ожидается один найденный фильм");

        FilmRequest filmRequest2 = new FilmRequest("Test Film 2", "Description of Test Film 2",
                90, LocalDate.of(2020, 1, 1), new Mpa(), List.of(new Genre()), List.of(new Director()));
        Mpa mpa = new Mpa();
        mpa.setId(2);
        filmRequest2.setMpa(mpa);
        Genre genre = new Genre();
        genre.setId(2);
        filmRequest2.setGenres(List.of(genre));
        Director director = new Director();
        director.setName("Christopher Nolan");
        director.setId(2);
        directorStorage.createDirector(director);
        filmRequest2.setDirectors(List.of(director));
        filmController.createFilm(filmRequest2);
        assertEquals(2, filmController.findAll().size(), "Ожидается два найденных фильма");
    }

    @Test
    void testCreateFilmWorksCorrectly() {
        FilmDto createdFilmDto = filmController.createFilm(filmRequest);
        FilmDto requestFilmDto =  FilmMapper.mapToFilmDto(FilmMapper.mapToFilm(filmRequest), mpaStorage, genreStorage, directorStorage);
        requestFilmDto.setId(createdFilmDto.getId());
        assertEquals(requestFilmDto, createdFilmDto, "Ожидается создание фильма с корректными данными");
    }

    @Test
    public void testUpdateExistingFilmWorksCorrectly() {
        filmController.createFilm(filmRequest);
        FilmRequestUpdate filmRequestUpdate = new FilmRequestUpdate("Test Film Updated", "Description of Test Film Updated", 90, LocalDate.of(2020, 1, 1), new Mpa(), List.of(new Genre()), 1, List.of(new Director()));
        Mpa mpaUpdate = new Mpa();
        mpaUpdate.setId(2);
        mpaUpdate.setName("PG");
        filmRequestUpdate.setMpa(mpaUpdate);

        Genre genreUpdate = new Genre();
        genreUpdate.setId(2);
        genreUpdate.setName("Драма");
        filmRequestUpdate.setGenres(List.of(genreUpdate));

        Director directorUpdate = new Director();
        directorUpdate.setName("Christopher Nolan");
        directorUpdate.setId(2);
        directorStorage.createDirector(directorUpdate);
        filmRequestUpdate.setDirectors(List.of(directorUpdate));

        filmController.updateFilm(filmRequestUpdate);
        assertThat(filmController.findFilmById(1))
                .as("Ожидается, что фильм с id 1 корректно обновлен")
                .hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("name", "Test Film Updated")
                .hasFieldOrPropertyWithValue("description", "Description of Test Film Updated")
                .hasFieldOrPropertyWithValue("duration", 90)
                .hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(2020, 1, 1))
                .hasFieldOrPropertyWithValue("mpa", mpaUpdate)
                .hasFieldOrPropertyWithValue("genres", List.of(genreUpdate));
    }

    @Test
    public void testUpdateNonExistingFilmWorksCorrectly() {
        FilmRequestUpdate filmRequestUpdate = new FilmRequestUpdate("NonExistingFilm", "NonExistingFilmDescription",
                90, LocalDate.of(2020, 1, 1), new Mpa(), List.of(new Genre()), 1, List.of(new Director()));
        assertThrows(NotFoundException.class, () -> filmController.updateFilm(filmRequestUpdate),
                "Ожидается исключение NotFoundException при попытке обновления несуществующего фильма");
    }

    @Test
    public void testAddLikeToFilmWorksCorrectly() {
        filmController.createFilm(filmRequest);
        UserController userController = new UserController(new UserService(userStorage));
        UserRequest userRequest = new UserRequest("testLogin1", "testLogin1@example.com", "testName1", LocalDate.of(2000, 1, 1));
        userController.createUser(userRequest);

        filmController.addLikeToFilm(1, 1);

        Integer likeCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM _like WHERE film_id = " + 1, Integer.class);
        assertEquals(1, likeCount, "Ожидается, что у фильма 1 есть 1 лайк");

        filmController.removeLikeFromFilm(1, 1);
        likeCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM _like WHERE film_id = " + 1, Integer.class);
        assertEquals(0, likeCount, "Ожидается, что у фильма 1 нет лайков");
    }

    @Test
    public void testGetPopularFilmsWorksCorrectly() {
        FilmRequest filmRequest2 = new FilmRequest("Test Film 2", "Description of Test Film 2",
                120, LocalDate.of(2022, 1, 1), null, null, null);
        Mpa mpa = new Mpa();
        mpa.setId(2);
        filmRequest2.setMpa(mpa);

        Genre genre = new Genre();
        genre.setId(2);
        filmRequest2.setGenres(List.of(genre));

        Director director = new Director();
        director.setId(2);
        director.setName("Christopher Nolan");
        directorStorage.createDirector(director);
        filmRequest2.setDirectors(List.of(director));

        FilmRequest filmRequest3 = new FilmRequest("Test Film 3", "Description of Test Film 3",
                120, LocalDate.of(2022, 1, 1), null, null, null);
        mpa = new Mpa();
        mpa.setId(2);
        filmRequest3.setMpa(mpa);
        genre = new Genre();
        genre.setId(2);
        filmRequest3.setGenres(List.of(genre));

        director = new Director();
        director.setId(3);
        director.setName("Ridley Scott");
        directorStorage.createDirector(director);
        filmRequest3.setDirectors(List.of(director));

        filmController.createFilm(filmRequest);
        filmController.createFilm(filmRequest2);
        filmController.createFilm(filmRequest3);
        UserController userController = new UserController(new UserService(userStorage));
        UserRequest userRequest = new UserRequest("testLogin1", "testLogin1@example.com", "testName1", LocalDate.of(2000, 1, 1));
        userController.createUser(userRequest);

        assertEquals(0, filmController.getPopularFilms(10).size(), "Ожидается, что список пустой при отсутствии лайков");

        filmController.addLikeToFilm(1, 1);
        filmController.addLikeToFilm(2, 1);
        filmController.addLikeToFilm(3, 1);
        filmController.getPopularFilms(10);
        assertEquals(3, filmController.getPopularFilms(10).size(), "Ожидается, что в списке теперь есть 3 фильма");

        filmController.removeLikeFromFilm(1, 1);
        assertEquals(2, filmController.getPopularFilms(10).size(), "Ожидается, что в списке осталось 2 фильма");
    }

}