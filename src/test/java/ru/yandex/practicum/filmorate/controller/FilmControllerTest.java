package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FilmControllerTest {

    @Autowired
    private FilmController filmController;
    private UserController userController;

    private Film film;

    @BeforeEach
    public void setUp() {
        UserService userService = new UserService(new InMemoryUserStorage());
        userController = new UserController(userService);
        filmController = new FilmController(new FilmService(new InMemoryFilmStorage(),userService));
        film = new Film();
        film.setId(1L);
        film.setName("Test Film");
        film.setDescription("Description of Test Film");
        film.setReleaseDate(LocalDate.of(2022, 1, 1));
        film.setDuration(90);
    }

    @Test
    public void testSearchFilmsWorksCorrectly() {
        filmController.create(film);
        assertEquals(1, filmController.findAll().size(), "Ожидается один найденный фильм");
        Film newFilm  = new Film();
        newFilm.setId(2L);
        newFilm.setName("Test 2 Film");
        newFilm.setDescription("Description of Test 2 Film");
        newFilm.setReleaseDate(LocalDate.of(2021, 1, 1));
        newFilm.setDuration(120);
        filmController.create(newFilm);
        assertEquals(2, filmController.findAll().size(), "Ожидается два найденных фильма");
    }

    @Test
    void testCreateFilm() {
        Film createdFilm = filmController.create(film);
        assertEquals(film, createdFilm, "Ожидается корректное создание фильма");
    }

    @Test
    public void testUpdateExistingFilm() {
        filmController.create(film);
        Film updatedFilm  = new Film();
        updatedFilm.setId(1L);
        updatedFilm.setName("Updated Film");
        updatedFilm.setDescription("Updated Description");
        updatedFilm.setReleaseDate(LocalDate.of(2022, 1, 1));
        updatedFilm.setDuration(120);
        Film resultFilm = filmController.update(updatedFilm);
        assertEquals("Updated Film", resultFilm.getName(), "Ожидается, что название фильма обновлено");
        assertEquals("Updated Description", resultFilm.getDescription(), "Ожидается, что описание фильма обновлено");
    }

    @Test
    public void testUpdateNonExistingFilm() {
        assertThrows(NotFoundException.class, () -> filmController.update(film),
                "Ожидается исключение NotFoundException при попытке обновления несуществующего фильма");
    }

    @Test
    public void testAddLikeToFilm() {
        filmController.create(film);
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setLogin("testLogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        userController.create(user);
        // Тестирование добавления лайка фильму
        filmController.addLikeToFilm(1L, 1L);
        assertEquals(1, film.getLikes().size(), "Ожидается, что у фильма 1 есть один лайк");
        // Тестирование удаления лайка фильму
        filmController.removeLikeFromFilm(1L, 1L);
        assertEquals(0, film.getLikes().size(), "Ожидается, что у фильма 1 не осталось лайков");
    }

    @Test
    public void testGetPopularFilms() {
        Film film1 = new Film();
        film1.setId(1L);
        film1.setName("Film 1");
        film1.setDescription("Description of Film 1");
        film1.setReleaseDate(LocalDate.of(2022, 1, 1));
        film1.setDuration(120);
        Film film2 = new Film();
        film2.setId(2L);
        film2.setName("Film 2");
        film2.setDescription("Description of Film 2");
        film2.setReleaseDate(LocalDate.of(2021, 1, 1));
        film2.setDuration(65);
        Film film3 = new Film();
        film3.setId(3L);
        film3.setName("Film 3");
        film3.setDescription("Description of Film 3");
        film3.setReleaseDate(LocalDate.of(2020, 1, 1));
        film3.setDuration(90);
        filmController.create(film1);
        filmController.create(film2);
        filmController.create(film3);
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setLogin("testLogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        userController.create(user);
        // Проверяем, что список пустой при отсутствии лайков
        assertEquals(0, filmController.getPopularFilms(10).size(), "Ожидается, что список пустой при отсутствии лайков");
        filmController.addLikeToFilm(1L, 1L);
        filmController.addLikeToFilm(2L, 1L);
        filmController.addLikeToFilm(3L, 1L);
        filmController.getPopularFilms(10);
        // Проверяем, что список не пустой после добавления лайков
        assertEquals(3, filmController.getPopularFilms(10).size(), "Ожидается, что в списке есть 3 фильма");
        filmController.removeLikeFromFilm(1L, 1L);
        // Проверяем, что список уменьшился после удаления лайка
        assertEquals(2, filmController.getPopularFilms(10).size(), "Ожидается, что в списке есть 2 фильма");
    }

}