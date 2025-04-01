package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FilmControllerTest {

    @Autowired
    private FilmController filmController;

    @BeforeEach
    public void setUp() {
        clearFilms();
        Film film = new Film();
        film.setId(1L);
        film.setName("Test Film");
        film.setDescription("Description of Test Film");
        film.setReleaseDate(LocalDate.of(2022, 1, 1));
        film.setDuration(120);
        filmController.create(film);
    }

    private void clearFilms() {
        filmController.getFilms().clear();
    }

    @Test
    public void testFindAllFilms() {
        assertEquals(1, filmController.findAll().size(), "Изначально должен быть один фильм");
    }

    @Test
    public void testCreateFilmWithUniqueName() {
        Film newFilm = new Film();
        newFilm.setName("Unique Film");
        newFilm.setDescription("Unique Description");
        newFilm.setReleaseDate(LocalDate.of(2023, 1, 1));
        newFilm.setDuration(150);
        Film createdFilm = filmController.create(newFilm);
        assertNotNull(createdFilm.getId(), "ID нового фильма не должен быть null");
        assertEquals(2, filmController.findAll().size(), "Должно быть два фильма");
        assertEquals("Unique Film", createdFilm.getName(), "Имя должно совпадать");
    }

    @Test
    public void testCreateFilmWithDuplicateName() {
        Film duplicateFilm = new Film();
        duplicateFilm.setName("Test Film");
        duplicateFilm.setDescription("Another Description");
        duplicateFilm.setReleaseDate(LocalDate.of(2021, 1, 1));
        duplicateFilm.setDuration(100);
        assertThrows(ValidationException.class, () -> filmController.create(duplicateFilm),
                "Ожидается исключение ValidationException из-за дублирующегося имени");
    }

    @Test
    public void testCreateFilmWithInvalidReleaseDate() {
        Film invalidFilm = new Film();
        invalidFilm.setName("Invalid Film");
        invalidFilm.setDescription("Invalid Description");
        invalidFilm.setReleaseDate(LocalDate.of(1890, 1, 1)); // Дата до 28.12.1895
        invalidFilm.setDuration(90);
        assertThrows(ValidationException.class, () -> filmController.create(invalidFilm),
                "Ожидается исключение ValidationException из-за некорректной даты выхода");
    }

    @Test
    public void testUpdateExistingFilm() {
        Film updatedFilm = new Film();
        updatedFilm.setId(1L);
        updatedFilm.setName("Updated Film");
        updatedFilm.setDescription("Updated Description");
        updatedFilm.setReleaseDate(LocalDate.of(2022, 1, 1));
        updatedFilm.setDuration(130);
        Film result = filmController.update(updatedFilm);
        assertEquals("Updated Film", result.getName(), "Имя должно быть обновлено");
        assertEquals("Updated Description", result.getDescription(), "Описание должно быть обновлено");
        assertEquals(130, result.getDuration(), "Длительность должна быть обновлена");
    }

    @Test
    public void testUpdateNonExistingFilm() {
        Film nonExistingFilm = new Film();
        nonExistingFilm.setId(99L);
        nonExistingFilm.setName("Non-Existing Film");
        nonExistingFilm.setDescription("Non-Existing Description");
        nonExistingFilm.setReleaseDate(LocalDate.of(2020, 1, 1));
        nonExistingFilm.setDuration(100);
        assertThrows(NotFoundException.class, () -> filmController.update(nonExistingFilm),
                "Ожидается исключение NotFoundException при попытке обновления несуществующего фильма");
    }

    @Test
    public void testUpdateFilmWithDuplicateName() {
        Film anotherFilm = new Film();
        anotherFilm.setName("Another Film");
        anotherFilm.setDescription("Another Description");
        anotherFilm.setReleaseDate(LocalDate.of(2023, 2, 1));
        anotherFilm.setDuration(140);
        filmController.create(anotherFilm);
        Film updatedFilm = new Film();
        updatedFilm.setId(1L);
        updatedFilm.setName("Another Film"); // Дублируемое имя
        updatedFilm.setDescription("Updated Description");
        updatedFilm.setReleaseDate(LocalDate.of(2022, 1, 1));
        updatedFilm.setDuration(130);
        assertThrows(ValidationException.class, () -> filmController.update(updatedFilm),
                "Ожидается исключение ValidationException из-за дублирующегося имени");
    }
}