
package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {

    User createUser(User user);

    User findUserById(Long userId);

    User updateUser(User newUser);

    void deleteUser(Long userId);

    Collection<User> findAll();
}
