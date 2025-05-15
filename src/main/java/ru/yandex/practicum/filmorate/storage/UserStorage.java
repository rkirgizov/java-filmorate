
package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.dto.UserFriendDto;
import ru.yandex.practicum.filmorate.model.User;

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

}
