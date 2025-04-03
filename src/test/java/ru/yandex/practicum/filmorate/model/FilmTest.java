package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FilmTest {

    @Autowired
    private Validator validator;

    private Film film;

    @BeforeEach
    public void setUp() {
        film = new Film();
    }

    @Test
    void validationNameWorkCorrectly() {
        film.setDescription("Film description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        // name is null
        film.setName(null);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(),"Ожидается ошибка валидации из-за null в имени");
        // name is blank
        film.setName("");
        violations = validator.validate(film);
        assertFalse(violations.isEmpty(),"Ожидается ошибка валидации из-за пустого имени");
    }

    @Test
    void validationDescriptionWorkCorrectly() {
        film.setName("Film Name");
        // description is too long
        film.setDescription("A".repeat(201));
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(),"Ожидается ошибка валидации из-за слишком длинного описания");
    }

    @Test
    void validationReleaseDateWorkCorrectly() {
        film.setName("Film Name");
        film.setDescription("Film description");
        film.setDuration(120);
        // date is in the future
        film.setReleaseDate(LocalDate.now().plusDays(1));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(),"Ожидается ошибка валидации из-за даты релиза в будущем");
    }

    @Test
    void validationDurationDateWorkCorrectly() {
        film.setName("Film Name");
        film.setDescription("Film description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        // duration is null
        film.setDuration(null);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(),"Ожидается ошибка валидации из-за null в длительности");
        // duration is not positive
        film.setDuration(0);
        violations = validator.validate(film);
        assertFalse(violations.isEmpty(),"Ожидается ошибка валидации из-за не положительной длительности");
    }

    @Test
    void validationPassCorrectly() {
        film.setName("Film Name");
        film.setDescription("Film description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty(),"Ожидается отсутствие ошибок валидации");
    }
}