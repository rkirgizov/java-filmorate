package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.rowMapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.impl.GenreStorageDbImpl;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@JdbcTest
@AutoConfigureTestDatabase
@Import({GenreStorageDbImpl.class,
        GenreRowMapper.class})
public class GenreControllerTest {

    @Autowired
    private GenreStorage genreStorage;

    private GenreController genreController;

    @BeforeEach
    public void setUp() {
        genreController = new GenreController(new GenreService(genreStorage));
    }

    @Test
    public void testSearchGenresWorksCorrectly() {

        assertEquals(6, genreController.findAll().size(), "Ожидается 6 предопределенных жанров");

        assertThat(genreController.findGenreById(1))
        .as("Ожидается, что жанр с id 1 c корректными данными")
                .hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("name", "Комедия");
    }

}