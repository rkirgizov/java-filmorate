package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.dto.UserRequest;
import ru.yandex.practicum.filmorate.dto.UserRequestUpdate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.impl.UserStorageDbImpl;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserStorageDbImpl.class, UserRowMapper.class})
public class UserControllerTest {

    @Autowired
    private UserStorage userStorage;

    private UserController userController;
    private UserRequest userRequest;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "_user", "_user_friend");
        jdbcTemplate.execute("ALTER TABLE _user ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE _user_friend ALTER COLUMN id RESTART WITH 1");
        userController = new UserController(new UserService(userStorage));
        userRequest = new UserRequest("testLogin1", "testLogin1@example.com", "testName1", LocalDate.of(2000, 1, 1));
    }

    @Test
    public void testSearchUsersWorksCorrectly() {
        userController.createUser(userRequest);
        assertEquals(1, userController.findAllUsers().size(), "Ожидается один найденный пользователь");

        userRequest = new UserRequest("testLogin2", "testLogin2@example.com", "testName2", LocalDate.of(2000, 1, 1));
        userController.createUser(userRequest);
        assertEquals(2, userController.findAllUsers().size(), "Ожидается два найденных пользователя");
    }

    @Test
    void testCreateUserWorksCorrectly() {
        UserDto createdUserDto = userController.createUser(userRequest);
        UserDto requestUserDto =  UserMapper.mapToUserDto(UserMapper.mapToUser(userRequest));
        requestUserDto.setId(createdUserDto.getId());
        assertEquals(requestUserDto, createdUserDto, "Ожидается создание пользователя с корректными данными");
    }

    @Test
    public void testRemoveUserWorksCorrectly() {
        userController.createUser(userRequest);
        userController.removeUser(1);
        assertThrows(NotFoundException.class, () -> userController.findUserById(1),
                "Ожидается исключение NotFoundException при попытке найти удаленного пользователя");
    }

    @Test
    public void testUpdateExistingUserWorksCorrectly() {
        UserDto userDto = userController.createUser(userRequest);
        int userId = userDto.getId();
        UserRequestUpdate userRequestUpdate = new UserRequestUpdate(userId, "testLogin1Updated", "testLogin1Updated@example.com",
                "testName1Updated", LocalDate.of(1980, 1, 1));
        userController.updateUser(userRequestUpdate);
        UserDto updateUserDto = userController.findUserById(userId);
        assertEquals("testLogin1Updated@example.com", updateUserDto.getEmail(), "Ожидается, что Email пользователя обновлен");
        assertEquals("testName1Updated", updateUserDto.getName(), "Ожидается, что Имя пользователя обновлено");
        assertEquals("testLogin1Updated", updateUserDto.getLogin(), "Ожидается, что Логин пользователя обновлен");
    }

    @Test
    public void testUpdateNonExistingUserWorksCorrectly() {
        UserRequestUpdate userRequestUpdate = new UserRequestUpdate(1, "NonExistingUser", "NonExistingUser@example.com",
                "NonExistingUserName", LocalDate.of(1980, 1, 1));
        assertThrows(NotFoundException.class, () -> userController.updateUser(userRequestUpdate),
                "Ожидается исключение NotFoundException при попытке обновления несуществующего пользователя");
    }

    @Test
    public void testAddAndRemoveFriendsWorksCorrectly() {
        userController.createUser(userRequest);
        UserRequest userRequestFriend = new UserRequest("testLogin2", "testLogin2@example.com", "testName2", LocalDate.of(1990, 1, 1));
        userController.createUser(userRequestFriend);
        userController.addFriendRequest(1, 2);
        assertThat(userController.findFriendsByUserId(1))
                .as("Ожидается, что пользователь с id 1 имеет одного друга с id 2")
                .isNotEmpty()
                .hasSize(1)
                .first()
                .hasFieldOrPropertyWithValue("id", 2)
                .hasFieldOrPropertyWithValue("login", "testLogin2");
        assertThat(userController.findFriendsByUserId(2))
                .as("Ожидается, что пользователь с id 2 не имеет друзей (однонаправленное добавление друзей)")
                .isEmpty();

        userController.removeFromFriends(1, 2);
        assertThat(userController.findFriendsByUserId(1))
                .as("Ожидается, что пользователь с id 2 удален из друзей пользователя с id 1")
                .isEmpty();
    }

    @Test
    public void testGetCommonFriendsListWorksCorrectly() {
        UserRequest userRequest2 = new UserRequest("testLogin2", "testLogin2@example.com", "testName2", LocalDate.of(1990, 1, 1));
        UserRequest userRequest3 = new UserRequest("testLogin3", "testLogin3@example.com", "testName3", LocalDate.of(1980, 1, 1));
        userController.createUser(userRequest);
        userController.createUser(userRequest2);
        userController.createUser(userRequest3);
        userController.addFriendRequest(1, 3);
        userController.addFriendRequest(2, 3);
        assertThat(userController.findCommonFriends(1, 2))
                .as("Ожидается, что пользователь c id 1 и 2 имеют общего друга с id 3")
                        .isNotEmpty()
                .hasSize(1)
                .first()
                .hasFieldOrPropertyWithValue("id", 3)
                .hasFieldOrPropertyWithValue("login", "testLogin3");
    }

}