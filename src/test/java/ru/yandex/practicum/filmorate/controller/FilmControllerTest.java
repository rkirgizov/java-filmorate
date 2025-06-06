package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.FilmRequest;
import ru.yandex.practicum.filmorate.dto.FilmRequestUpdate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.rowMapper.*;
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
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "_film_genre", "_like", "_film", "_user", "_film_director", "_director");
        jdbcTemplate.execute("ALTER TABLE _film ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE _user ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE _director ALTER COLUMN id RESTART WITH 1");

        filmController = new FilmController(filmService);
        userController = new UserController(userService);

        Mpa mpaSetup = new Mpa();
        mpaSetup.setId(1);
        mpaSetup.setName("G");

        Genre genreSetup = new Genre();
        genreSetup.setId(1);
        genreSetup.setName("Комедия");

        Director directorSetup = new Director();
        directorSetup.setId(1);
        directorSetup.setName("Quentin Tarantino");
        directorStorage.createDirector(directorSetup);

        filmRequest = new FilmRequest(
                "Test Film",
                "Description of Test Film",
                120,
                LocalDate.of(2022, 1, 1),
                mpaSetup,
                List.of(genreSetup),
                List.of(directorSetup)
        );
    }

    @Test
    void testCreateFilmWorksCorrectly() {
        FilmDto createdFilmDto = filmController.createFilm(filmRequest);

        FilmRequest expectedRequest = new FilmRequest("Test Film", "Description of Test Film", 120, LocalDate.of(2022, 1, 1), new Mpa(), List.of(new Genre()), List.of(new Director()));
        expectedRequest.getMpa().setId(1);
        expectedRequest.getGenres().get(0).setId(1);
        expectedRequest.getDirectors().get(0).setId(1);
        expectedRequest.getDirectors().get(0).setName("Quentin Tarantino");

        Film expectedFilm = FilmMapper.mapToFilm(expectedRequest);
        expectedFilm.setId(createdFilmDto.getId());

        FilmDto expectedFilmDto = FilmMapper.mapToFilmDto(expectedFilm, mpaStorage, genreStorage, directorStorage);


        assertEquals(expectedFilmDto, createdFilmDto);
    }


    @Test
    public void testUpdateExistingFilmWorksCorrectly() {
        filmController.createFilm(filmRequest);

        Mpa mpaUpdate = new Mpa();
        mpaUpdate.setId(2);
        mpaUpdate.setName("PG");

        Genre genreUpdate = new Genre();
        genreUpdate.setId(2);
        genreUpdate.setName("Драма");

        Director directorUpdate = new Director();
        directorUpdate.setId(2);
        directorUpdate.setName("Christopher Nolan");
        directorStorage.createDirector(directorUpdate);

        FilmRequestUpdate filmRequestUpdate = new FilmRequestUpdate(
                "Test Film Updated",
                "Description of Test Film Updated",
                90,
                LocalDate.of(2020, 1, 1),
                mpaUpdate,
                List.of(genreUpdate),
                1,
                List.of(directorUpdate)
        );

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
        Mpa mpaUpdate = new Mpa();
        mpaUpdate.setId(1);
        mpaUpdate.setName("G");
        Genre genreUpdate = new Genre();
        genreUpdate.setId(1);
        genreUpdate.setName("Комедия");
        Director directorUpdate = new Director();
        directorUpdate.setId(1);
        directorUpdate.setName("Director A");

        FilmRequestUpdate filmRequestUpdate = new FilmRequestUpdate(
                "NonExistingFilm", "NonExistingFilmDescription", 90, LocalDate.of(2020, 1, 1),
                mpaUpdate, List.of(genreUpdate), 1, List.of(directorUpdate)
        );

        assertThrows(NotFoundException.class, () -> filmController.updateFilm(filmRequestUpdate),
                "Ожидается исключение NotFoundException при попытке обновления несуществующего фильма");
    }
}