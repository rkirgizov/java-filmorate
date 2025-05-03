package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FilmTest {

    private FilmController filmController;
    private Film film;

    @BeforeEach
    public void setUp() {
        filmController = new FilmController(new FilmService(new InMemoryFilmStorage(),new UserService(new InMemoryUserStorage())));
        film = new Film();
        film.setId(1L);
        film.setName("Test Film");
        film.setDescription("Description of Test Film");
        film.setReleaseDate(LocalDate.of(2022, 1, 1));
    }

    @Test
    void validationNameWorkCorrectly() {
        film.setName(null);
        assertEquals("Название фильма не заполнено", assertThrows(ValidationException.class, () -> filmController.create(film)).getMessage(), "Ожидается ошибка валидации из-за null в name");
        film.setName(" ");
        assertEquals("Название фильма не заполнено", assertThrows(ValidationException.class, () -> filmController.create(film)).getMessage(), "Ожидается ошибка валидации из-за пустого значения в name");
    }

    @Test
    void validationDescriptionWorkCorrectly() {
        film.setDescription("A".repeat(201));
        assertEquals("Описание фильма не должно превышать 200 символов", assertThrows(ValidationException.class, () -> filmController.create(film)).getMessage(), "Ожидается ошибка валидации из-за слишком длинного описания");
    }

    @Test
    void validationReleaseDateWorkCorrectly() {
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        assertEquals("Дата релиза не может быть раньше 28 декабря 1895 года", assertThrows(ValidationException.class, () -> filmController.create(film)).getMessage(), "Ожидается ошибка валидации из-за даты релиза раньше 1895");
    }

    @Test
    void validationDurationDateWorkCorrectly() {
        film.setDuration(null);
        assertEquals("Продолжительность фильма должна быть положительным числом", assertThrows(ValidationException.class, () -> filmController.create(film)).getMessage(), "Ожидается ошибка валидации из-за null в длительности");
        film.setDuration(0);
        assertEquals("Продолжительность фильма должна быть положительным числом", assertThrows(ValidationException.class, () -> filmController.create(film)).getMessage(), "Ожидается ошибка валидации из-за длительности равной нулю");
        film.setDuration(-1);
        assertEquals("Продолжительность фильма должна быть положительным числом", assertThrows(ValidationException.class, () -> filmController.create(film)).getMessage(), "Ожидается ошибка валидации из-за отрицательной длительности");
    }

}