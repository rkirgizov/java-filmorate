package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.dto.FilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.*;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.*;
import ru.yandex.practicum.filmorate.storage.impl.*;

import java.time.LocalDate;
import java.util.List;

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
public class FilmTest {

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
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "_user");
        jdbcTemplate.execute("ALTER TABLE _user ALTER COLUMN id RESTART WITH 1");
        filmController = new FilmController(new FilmService(filmStorage, mpaStorage, genreStorage, directorStorage, userStorage));
        filmRequest = new FilmRequest("Test Film", "Description of Test Film",
                120, LocalDate.of(2022, 1, 1), new Mpa(), List.of(new Genre()), List.of(new Director()));
    }

    @Test
    void validationNameWorkCorrectly() {
        filmRequest.setName(null);
        assertEquals("Название фильма не заполнено",
                assertThrows(ValidationException.class, () -> filmController.createFilm(filmRequest)).getMessage(),
                "Ожидается ошибка валидации из-за null в name");
        filmRequest.setName(" ");

        assertEquals("Название фильма не заполнено",
                assertThrows(ValidationException.class, () -> filmController.createFilm(filmRequest)).getMessage(),
                "Ожидается ошибка валидации из-за пустого значения в name");
    }

    @Test
    void validationDescriptionWorkCorrectly() {
        filmRequest.setDescription("A".repeat(201));
        assertEquals("Описание фильма не должно превышать 200 символов",
                assertThrows(ValidationException.class, () -> filmController.createFilm(filmRequest)).getMessage(),
                "Ожидается ошибка валидации из-за слишком длинного описания");
    }

    @Test
    void validationReleaseDateWorkCorrectly() {
        filmRequest.setReleaseDate(LocalDate.of(1895, 12, 27));
        assertEquals("Дата релиза не может быть раньше 28 декабря 1895 года",
                assertThrows(ValidationException.class, () -> filmController.createFilm(filmRequest)).getMessage(),
                "Ожидается ошибка валидации из-за даты релиза раньше 1895");
    }

    @Test
    void validationDurationDateWorkCorrectly() {
        filmRequest.setDuration(null);
        assertEquals("Продолжительность фильма не заполнена",
                assertThrows(ValidationException.class, () -> filmController.createFilm(filmRequest)).getMessage(),
                "Ожидается ошибка валидации из-за null в длительности");
        filmRequest.setDuration(0);
        assertEquals("Продолжительность фильма должна быть положительным числом",
                assertThrows(ValidationException.class, () -> filmController.createFilm(filmRequest)).getMessage(),
                "Ожидается ошибка валидации из-за длительности равной нулю");
        filmRequest.setDuration(-1);
        assertEquals("Продолжительность фильма должна быть положительным числом",
                assertThrows(ValidationException.class, () -> filmController.createFilm(filmRequest)).getMessage(),
                "Ожидается ошибка валидации из-за отрицательной длительности");
    }

    @Test
    void validationFailMpaWorkCorrectly() {
        Mpa mpa = new Mpa();
        mpa.setId(100);
        filmRequest.setMpa(mpa);
        assertEquals("Рейтинг МПА с id = 100 не найден в справочнике",
                assertThrows(NotFoundException.class, () -> filmController.createFilm(filmRequest)).getMessage(),
                "Ожидается ошибка валидации NotFoundException из-за неизвестного рейтинга МПА");
    }

    @Test
    void validationFailGenreWorkCorrectly() {
        Mpa mpa = new Mpa();
        mpa.setId(1);
        filmRequest.setMpa(mpa);
        Genre genre = new Genre();
        genre.setId(100);
        filmRequest.setGenres(List.of(genre));
        assertEquals("Жанр с id = 100 не найден в справочнике",
                assertThrows(NotFoundException.class, () -> filmController.createFilm(filmRequest)).getMessage(),
                "Ожидается ошибка валидации NotFoundException из-за неизвестного жанра");
    }


}