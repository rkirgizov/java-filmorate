package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.rowMapper.MpaRowMapper;
import ru.yandex.practicum.filmorate.service.MpaService;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.impl.MpaStorageDbImpl;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@JdbcTest
@AutoConfigureTestDatabase
@Import({MpaStorageDbImpl.class,
        MpaRowMapper.class})
public class MpaControllerTest {

    @Autowired
    private MpaStorage mpaStorage;

    private MpaController mpaController;

    @BeforeEach
    public void setUp() {
        mpaController = new MpaController(new MpaService(mpaStorage));
    }

    @Test
    public void testSearchMpasWorksCorrectly() {

        assertEquals(5, mpaController.findAll().size(), "Ожидается 5 предопределенных возрастных рейтингов");

        assertThat(mpaController.findMpaById(1))
        .as("Ожидается, что рейтинг с id 1 c корректными данными")
                .hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("name", "G");
    }

}