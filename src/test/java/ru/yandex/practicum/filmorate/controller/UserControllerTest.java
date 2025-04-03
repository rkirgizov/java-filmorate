package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserControllerTest {

    @Autowired
    private UserController userController;

    @BeforeEach
    public void setUp() {
        clearUsers();
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setLogin("testLogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        userController.create(user);
    }

    private void clearUsers() {
        userController.getUsers().clear();
    }

    @Test
    public void testFindAllUsers() {
        assertEquals(1, userController.findAll().size(), "Ожидается один пользователь");
    }

    @Test
    public void testCreateUserWithUniqueEmail() {
        User newUser = new User();
        newUser.setEmail("unique@example.com");
        newUser.setLogin("uniqueLogin");
        newUser.setName("Unique User");
        newUser.setBirthday(LocalDate.of(1990, 2, 15));
        User createdUser = userController.create(newUser);
        assertNotNull(createdUser.getId(), "ID нового пользователя не должен быть null");
        assertEquals(2, userController.findAll().size(), "Ожидается два пользователя");
    }

    @Test
    public void testCreateUserWithDuplicateEmail() {
        User duplicateEmailUser = new User();
        duplicateEmailUser.setEmail("test@example.com");
        duplicateEmailUser.setLogin("duplicateLogin");
        duplicateEmailUser.setName("Duplicate User");
        duplicateEmailUser.setBirthday(LocalDate.of(1995, 3, 10));
        assertThrows(ValidationException.class, () -> userController.create(duplicateEmailUser),
                "Ожидается исключение из-за дублирующегося email");
    }

    @Test
    public void testCreateUserWithNoName() {
        User noNameUser = new User();
        noNameUser.setEmail("noname@example.com");
        noNameUser.setLogin("noNameLogin");
        noNameUser.setBirthday(LocalDate.of(1993, 5, 25));
        User createdUser = userController.create(noNameUser);
        assertEquals(noNameUser.getLogin(), createdUser.getName(), "Имя должно быть установлено равным логину");
    }

    @Test
    public void testUpdateExistingUser() {
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setEmail("updated@example.com");
        updatedUser.setLogin("updatedLogin");
        updatedUser.setName("Updated User");
        updatedUser.setBirthday(LocalDate.of(2000, 1, 1));
        User result = userController.update(updatedUser);
        assertEquals("updated@example.com", result.getEmail(), "Email должен быть обновлен");
        assertEquals("Updated User", result.getName(), "Имя должно быть обновлено");
        assertEquals("updatedLogin", result.getLogin(), "Логин должен быть обновлен");
    }

    @Test
    public void testUpdateNonExistingUser() {
        User nonExistingUser = new User();
        nonExistingUser.setId(99L);
        nonExistingUser.setEmail("nonexisting@example.com");
        nonExistingUser.setLogin("nonexistingLogin");
        nonExistingUser.setName("Non-Existing User");
        nonExistingUser.setBirthday(LocalDate.of(1995, 7, 20));
        assertThrows(NotFoundException.class, () -> userController.update(nonExistingUser),
                "Ожидается исключение NotFoundException при попытке обновления несуществующего пользователя");
    }

    @Test
    public void testUpdateUserWithDuplicateEmail() {
        User anotherUser = new User();
        anotherUser.setEmail("another@example.com");
        anotherUser.setLogin("anotherLogin");
        anotherUser.setName("Another User");
        anotherUser.setBirthday(LocalDate.of(1980, 6, 30));
        userController.create(anotherUser);
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setEmail("another@example.com");
        updatedUser.setLogin("updatedLogin");
        updatedUser.setName("Updated User");
        updatedUser.setBirthday(LocalDate.of(2000, 1, 1));
        assertThrows(ValidationException.class, () -> userController.update(updatedUser),
                "Ожидается исключение ValidationException из-за дублирующегося email");
    }
}