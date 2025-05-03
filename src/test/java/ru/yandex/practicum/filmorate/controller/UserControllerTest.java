package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserControllerTest {

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
    public void testSearchUsersWorksCorrectly() {
        userController.create(user);
        assertEquals(1, userController.findAll().size(), "Ожидается один найденный пользователь");
        User newUser = new User();
        newUser.setId(2L);
        newUser.setEmail("test1@example.com");
        newUser.setLogin("test1Login");
        newUser.setName("Test 1 User");
        newUser.setBirthday(LocalDate.of(2000, 1, 1));
        userController.create(newUser);
        assertEquals(2, userController.findAll().size(), "Ожидается два найденных пользователя");
    }

    @Test
    void testCreateUser() {
        User createdUser = userController.create(user);
        assertEquals(user, createdUser, "Ожидается корректное создание пользователя");
    }

    @Test
    public void testUpdateExistingUser() {
        userController.create(user);
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setEmail("updated@example.com");
        updatedUser.setLogin("updatedLogin");
        updatedUser.setName("Updated User");
        updatedUser.setBirthday(LocalDate.of(2000, 1, 1));
        User resultUser = userController.update(updatedUser);
        assertEquals("updated@example.com", resultUser.getEmail(), "Ожидается, что Email пользователя обновлен");
        assertEquals("Updated User", resultUser.getName(), "Ожидается, что Имя пользователя обновлено");
        assertEquals("updatedLogin", resultUser.getLogin(), "Ожидается, что Логин пользователя обновлен");
    }

    @Test
    public void testUpdateNonExistingUser() {
        assertThrows(NotFoundException.class, () -> userController.update(user),
                "Ожидается исключение NotFoundException при попытке обновления несуществующего пользователя");
    }

    @Test
    public void testAddAndRemoveFriends() {
        userController.create(user);
        // Тестирование добавления друзей
        User friend = new User();
        friend.setId(2L);
        friend.setEmail("friend@example.com");
        friend.setLogin("friendLogin");
        friend.setName("Friend User");
        friend.setBirthday(LocalDate.of(2000, 1, 2));
        userController.create(friend);
        userController.addToFriends(1L, 2L);
        assertEquals(1, user.getFriends().size(), "Ожидается, что пользователь имеет одного друга");
        assertTrue(user.getFriends().contains(2L), "Ожидается, что пользователь 2 добавлен в друзья пользователя 1");
        assertTrue(friend.getFriends().contains(1L), "Ожидается, что пользователь 1 добавлен в друзья пользователя 2");
        // Тестирование удаления друзей
        userController.removeFromFriends(1L, 2L);
        assertFalse(user.getFriends().contains(2L), "Ожидается, что пользователь 2 удален из друзей пользователя 1");
        assertFalse(friend.getFriends().contains(1L), "Ожидается, что пользователь 1 удален из друзей пользователя 2");
    }

    @Test
    public void testGetCommonFriendsList() {
        User friend1 = new User();
        friend1.setId(2L);
        friend1.setEmail("friend1@example.com");
        friend1.setLogin("friend1Login");
        friend1.setName("Friend 1 User");
        friend1.setBirthday(LocalDate.of(2000, 1, 2));
        User friend2 = new User();
        friend2.setId(3L);
        friend2.setEmail("friend3@example.com");
        friend2.setLogin("friend3Login");
        friend2.setName("Friend 3 User");
        friend2.setBirthday(LocalDate.of(2000, 1, 3));
        userController.create(user);
        userController.create(friend1);
        userController.create(friend2);
        userController.addToFriends(1L, 2L);
        userController.addToFriends(1L, 3L);
        assertEquals(friend1.getFriends().contains(1L),friend2.getFriends().contains(1L), "Ожидается, что пользователь 1 является общим другом для пользователей 2 и 3");
    }

}