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
import java.util.Set;
import java.util.stream.Collectors;

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
        UserRowMapper.class,
        FilmService.class,
        UserService.class
})
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

    @Autowired
    private FilmService filmService;

    @Autowired
    private UserService userService;

    private FilmController filmController;
    private UserController userController;
    private FilmRequest filmRequest;


    @BeforeEach
    public void setUp() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "_film_genre", "_like", "_film", "_user");
        jdbcTemplate.execute("ALTER TABLE _film ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE _user ALTER COLUMN id RESTART WITH 1");

        filmController = new FilmController(new FilmService(filmStorage, mpaStorage, genreStorage, directorStorage, userStorage));
        userController = new UserController(userService);

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
        assertEquals(1, filmController.findAll().size());

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
        assertEquals(2, filmController.findAll().size());
    }

    @Test
    void testCreateFilmWorksCorrectly() {
        FilmDto createdFilmDto = filmController.createFilm(filmRequest);

        FilmDto requestFilmDto =  FilmMapper.mapToFilmDto(FilmMapper.mapToFilm(filmRequest), mpaStorage, genreStorage, directorStorage);
        requestFilmDto.setId(createdFilmDto.getId());
        assertEquals(requestFilmDto, createdFilmDto);
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
                90, LocalDate.of(2020, 1, 1), new Mpa(), List.of(new Genre()), 1);
        assertThrows(NotFoundException.class, () -> filmController.updateFilm(filmRequestUpdate));
    }

    @Test
    public void testAddLikeToFilmWorksCorrectly() {
        filmController.createFilm(filmRequest);
        UserRequest userRequest = new UserRequest("testLogin1", "testLogin1@example.com", "testName1", LocalDate.of(2000, 1, 1));
        userController.createUser(userRequest);

        filmController.addLikeToFilm(1, 1);

        Integer likeCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM _like WHERE film_id = " + 1, Integer.class);
        assertEquals(1, likeCount);

        filmController.removeLikeFromFilm(1, 1);
        likeCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM _like WHERE film_id = " + 1, Integer.class);
        assertEquals(0, likeCount);
    }

    @Test
    public void testGetPopularFilmsWorksCorrectly() {
        FilmRequest filmRequest2 = new FilmRequest("Test Film 2", "Description of Test Film 2",
                          
                120, LocalDate.of(2022, 1, 1), null, null);
        Mpa mpa2 = new Mpa();
        mpa2.setId(2);
        filmRequest2.setMpa(mpa2);
        Genre genre2 = new Genre();
        genre2.setId(2);
        filmRequest2.setGenres(List.of(genre2));

        Director director = new Director();
        director.setId(2);
        director.setName("Christopher Nolan");
        directorStorage.createDirector(director);
        filmRequest2.setDirectors(List.of(director));
      
        FilmRequest filmRequest3 = new FilmRequest("Test Film 3", "Description of Test Film 3",
                120, LocalDate.of(2022, 1, 1), null, null);
        Mpa mpa3 = new Mpa();
        mpa3.setId(2);
        filmRequest3.setMpa(mpa3);
        Genre genre3 = new Genre();
        genre3.setId(2);
        filmRequest3.setGenres(List.of(genre3));
      
        director = new Director();
        director.setId(3);
        director.setName("Ridley Scott");
        directorStorage.createDirector(director);
        filmRequest3.setDirectors(List.of(director));

        FilmDto createdFilm1 = filmController.createFilm(filmRequest);
        FilmDto createdFilm2 = filmController.createFilm(filmRequest2);
        FilmDto createdFilm3 = filmController.createFilm(filmRequest3);

        UserRequest userRequest1 = new UserRequest("testLogin1", "testLogin1@example.com", "testName1", LocalDate.of(2000, 1, 1));
        UserDto createdUser1 = userController.createUser(userRequest1);

        List<FilmDto> popularFilmsInitial = filmController.getPopularFilms(10);
        assertEquals(3, popularFilmsInitial.size());
        Set<Integer> initialFilmIds = popularFilmsInitial.stream().map(FilmDto::getId).collect(Collectors.toSet());
        Set<Integer> createdFilmIds = Set.of(createdFilm1.getId(), createdFilm2.getId(), createdFilm3.getId());
        assertEquals(createdFilmIds, initialFilmIds);

        filmController.addLikeToFilm(createdFilm1.getId(), createdUser1.getId());
        filmController.addLikeToFilm(createdFilm2.getId(), createdUser1.getId());
        filmController.addLikeToFilm(createdFilm3.getId(), createdUser1.getId());

        List<FilmDto> popularFilmsAfterLikes = filmController.getPopularFilms(10);
        assertEquals(3, popularFilmsAfterLikes.size());
        Set<Integer> afterLikeFilmIds = popularFilmsAfterLikes.stream().map(FilmDto::getId).collect(Collectors.toSet());
        assertEquals(createdFilmIds, afterLikeFilmIds);

        UserRequest userRequest2 = new UserRequest("testLogin2", "testLogin2@example.com", "testName2", LocalDate.of(2001, 1, 1));
        UserDto createdUser2 = userController.createUser(userRequest2);
        filmController.addLikeToFilm(createdFilm2.getId(), createdUser2.getId());

        List<FilmDto> popularFilmsSorted = filmController.getPopularFilms(10);
        assertEquals(3, popularFilmsSorted.size());
        assertEquals(createdFilm2.getId(), popularFilmsSorted.get(0).getId());
        Set<Integer> secondAndThirdFilmIds = Set.of(popularFilmsSorted.get(1).getId(), popularFilmsSorted.get(2).getId());
        Set<Integer> expectedSecondAndThirdFilmIds = Set.of(createdFilm1.getId(), createdFilm3.getId());
        assertEquals(expectedSecondAndThirdFilmIds, secondAndThirdFilmIds);

        filmController.removeLikeFromFilm(createdFilm1.getId(), createdUser1.getId());

        List<FilmDto> popularFilmsAfterRemove = filmController.getPopularFilms(10);
        assertEquals(3, popularFilmsAfterRemove.size());
        assertEquals(createdFilm2.getId(), popularFilmsAfterRemove.get(0).getId());
        assertEquals(createdFilm3.getId(), popularFilmsAfterRemove.get(1).getId());
        assertEquals(createdFilm1.getId(), popularFilmsAfterRemove.get(2).getId());

        List<FilmDto> popularFilmsLimited = filmController.getPopularFilms(2);
        assertEquals(2, popularFilmsLimited.size());
        assertEquals(createdFilm2.getId(), popularFilmsLimited.get(0).getId());
        assertEquals(createdFilm3.getId(), popularFilmsLimited.get(1).getId());

    }
}