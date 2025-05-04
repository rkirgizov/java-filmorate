package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserTest {

    private UserController userController;
    private User user;

    @BeforeEach
    public void setUp() {
        userController = new UserController(new UserService(new InMemoryUserStorage()));
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setLogin("testLogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(2000, 1, 1));
    }

    @Test
    public void validationNoNameShouldBeSetNameToLogin() {
        user.setName(null);
        User createdUser = userController.create(user);
        assertEquals(user.getLogin(), createdUser.getName(), "Ожидается, что при значении name == null, оно должно быть установлено равным логину");
        User newUser = new User();
        newUser.setId(2L);
        newUser.setEmail("test1@example.com");
        newUser.setLogin("test1Login");
        newUser.setName(" ");
        newUser.setBirthday(LocalDate.of(2000, 1, 2));
        createdUser = userController.create(newUser);
        assertEquals(newUser.getLogin(), createdUser.getName(), "Ожидается, что при пустом значении name, оно должно быть установлено равным логину");
    }

    @Test
    void validationEmailWorkCorrectly() {
        // Корректное значение email
        user.setEmail(null);
        assertEquals("Электронная почта не заполнена", assertThrows(ValidationException.class, () -> userController.create(user)).getMessage(), "Ожидается ошибка валидации из-за null в email");
        user.setEmail(" ");
        assertEquals("Электронная почта не заполнена", assertThrows(ValidationException.class, () -> userController.create(user)).getMessage(), "Ожидается ошибка валидации из-за пустой строки в email");
        user.setEmail("testexample.com");
        assertEquals("Электронная почта введена некорректно", assertThrows(ValidationException.class, () -> userController.create(user)).getMessage(), "Ожидается ошибка валидации из-за некорректного email");
        user.setEmail("test@examplecom");
        assertEquals("Электронная почта введена некорректно", assertThrows(ValidationException.class, () -> userController.create(user)).getMessage(), "Ожидается ошибка валидации из-за некорректного email");
        user.setEmail("testexamplecom");
        assertEquals("Электронная почта введена некорректно", assertThrows(ValidationException.class, () -> userController.create(user)).getMessage(), "Ожидается ошибка валидации из-за некорректного email");
        // Повторяющееся значение email
        user.setEmail("test@example.com");
        userController.create(user);
        user.setLogin("newTestLogin");
        assertEquals("Пользователь с таким email уже существует", assertThrows(ValidationException.class, () -> userController.create(user)).getMessage(), "Ожидается ошибка валидации из-за повторяющегося email");
    }

    @Test
    void validationLoginWorkCorrectly() {
        // Корректное значение login
        user.setLogin(null);
        assertEquals("Логин не заполнен", assertThrows(ValidationException.class, () -> userController.create(user)).getMessage(), "Ожидается ошибка валидации из-за null в login");
        user.setLogin(" ");
        assertEquals("Логин не заполнен", assertThrows(ValidationException.class, () -> userController.create(user)).getMessage(), "Ожидается ошибка валидации из-за пустой строки в login");
        user.setLogin("invalid Login");
        assertEquals("Логин не может содержать пробелы", assertThrows(ValidationException.class, () -> userController.create(user)).getMessage(), "Ожидается ошибка валидации из-за логина с пробелами");
    }

    @Test
    void validationBirthdayWorkCorrectly() {
        user.setBirthday(null);
        assertEquals("Дата рождения не заполнена", assertThrows(ValidationException.class, () -> userController.create(user)).getMessage(), "Ожидается ошибка валидации из-за null в birthday");
        user.setBirthday(LocalDate.now().plusDays(1));
        assertEquals("Дата рождения не может быть в будущем", assertThrows(ValidationException.class, () -> userController.create(user)).getMessage(), "Ожидается ошибка валидации из-за даты рождения в будущем");
    }

}