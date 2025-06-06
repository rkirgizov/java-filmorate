
package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.dto.UserFriendDto;
import ru.yandex.practicum.filmorate.enumeration.EventOperation;
import ru.yandex.practicum.filmorate.enumeration.EventType;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserEvent;

import java.util.List;
import java.util.Optional;

public interface UserStorage {

    List<User> findAllUsers();

    void addFriendRequest(int userId, int friendId);

    User createUser(User user);

    Optional<User> findUserById(int userId);

    User updateUser(User user);

    void deleteUser(int userId);

    void deleteFriend(int userId, int friendId);

    List<UserFriendDto> findFriendsByUserId(int userId);

    List<UserEvent> findEventsByUserId(int userId);

    void addEvent(Integer userId, EventType eventType, EventOperation operation, Integer entityId);

}