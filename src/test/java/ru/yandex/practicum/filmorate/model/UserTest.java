package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.dto.UserRequest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.impl.UserStorageDbImpl;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserStorageDbImpl.class, UserRowMapper.class})
public class UserTest {

    @Autowired
    private UserStorage userStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UserController userController;
    private UserRequest userRequest;

    @BeforeEach
    public void setUp() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "_user");
        jdbcTemplate.execute("ALTER TABLE _user ALTER COLUMN id RESTART WITH 1");
        userController = new UserController(new UserService(userStorage));
        userRequest = new UserRequest("testLogin1", "testLogin1@example.com", "testName1", LocalDate.of(2000, 1, 1));
    }

    @Test
    public void validationNoNameShouldBeSetNameToLogin() {
        userRequest.setName(null);
        userController.createUser(userRequest);
        UserDto userDtoOptional = userController.findUserById(1);
        assertEquals("testLogin1", userDtoOptional.getName(),
                "Ожидается, что name == null при валидации будет установлено равным логину");

        userRequest = new UserRequest("testLogin2", "testLogin2@example.com", " ", LocalDate.of(2000, 1, 1));
        userController.createUser(userRequest);
        userDtoOptional = userController.findUserById(2);
        assertEquals("testLogin2", userDtoOptional.getName(),
                "Ожидается, что name == \" \" при валидации будет установлено равным логину");
    }

    @Test
    void validationEmailWorkCorrectly() {
        userRequest.setEmail(null);
        assertEquals("Электронная почта не заполнена",
                assertThrows(ValidationException.class, () -> userController.createUser(userRequest)).getMessage(),
                "Ожидается ошибка валидации из-за null в email");

        userRequest.setEmail(" ");
        assertEquals("Электронная почта не заполнена",
                assertThrows(ValidationException.class, () -> userController.createUser(userRequest)).getMessage(),
                "Ожидается ошибка валидации из-за пустой строки в email");

        userRequest.setEmail("testLogin1example.com");
        assertEquals("Электронная почта введена некорректно",
                assertThrows(ValidationException.class, () -> userController.createUser(userRequest)).getMessage(),
                "Ожидается ошибка валидации из-за некорректного email");

        userRequest.setEmail("testLogin1@examplecom");
        assertEquals("Электронная почта введена некорректно",
                assertThrows(ValidationException.class, () -> userController.createUser(userRequest)).getMessage(),
                "Ожидается ошибка валидации из-за некорректного email");

        userRequest.setEmail("testLogin1examplecom");
        assertEquals("Электронная почта введена некорректно",
                assertThrows(ValidationException.class, () -> userController.createUser(userRequest)).getMessage(),
                "Ожидается ошибка валидации из-за некорректного email");

        userRequest.setEmail("testLogin1@example.com");
        userController.createUser(userRequest);
        userRequest.setLogin("testLogin2");
        assertEquals("Пользователь с таким login или email уже существует",
                assertThrows(ValidationException.class, () -> userController.createUser(userRequest)).getMessage(),
                "Ожидается ошибка валидации из-за повторяющегося email");
    }

    @Test
    void validationLoginWorkCorrectly() {
        userRequest.setLogin(null);
        assertEquals("Логин не заполнен",
                assertThrows(ValidationException.class, () -> userController.createUser(userRequest)).getMessage(),
                "Ожидается ошибка валидации из-за null в login");

        userRequest.setLogin(" ");
        assertEquals("Логин не заполнен",
                assertThrows(ValidationException.class, () -> userController.createUser(userRequest)).getMessage(),
                "Ожидается ошибка валидации из-за пустой строки в login");

        userRequest.setLogin("invalid Login");
        assertEquals("Логин не может содержать пробелы",
                assertThrows(ValidationException.class, () -> userController.createUser(userRequest)).getMessage(),
                "Ожидается ошибка валидации из-за логина с пробелами");
    }

    @Test
    void validationBirthdayWorkCorrectly() {
        userRequest.setBirthday(null);
        assertEquals("Дата рождения не заполнена",
                assertThrows(ValidationException.class, () -> userController.createUser(userRequest)).getMessage(),
                "Ожидается ошибка валидации из-за null в birthday");

        userRequest.setBirthday(LocalDate.now().plusDays(1));
        assertEquals("Дата рождения не может быть в будущем",
                assertThrows(ValidationException.class, () -> userController.createUser(userRequest)).getMessage(),
                "Ожидается ошибка валидации из-за даты рождения в будущем");
    }

}