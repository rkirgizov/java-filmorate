
package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.UserFriendDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Slf4j
@Component("UserStorageInMemory")
public class UserStorageInMemory implements UserStorage {

    private final Map<Integer, User> users = new HashMap<>();

    @Override
    public User createUser(User user) {
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.debug("Пользователь с id = {}  - добавлен", user);
        return user;
    }

    @Override
    public User updateUser(User updatedUser) {

        if (updatedUser.getId() == null) {
            throw new ValidationException("Для обновления пользователя необходимо указать id");
        }
        if (users.containsKey(updatedUser.getId())) {
            User oldUser = users.get(updatedUser.getId());
            oldUser.setEmail(updatedUser.getEmail());
            oldUser.setName(updatedUser.getName());
            oldUser.setLogin(updatedUser.getLogin());
            oldUser.setBirthday(updatedUser.getBirthday());
            return oldUser;
        }
        throw new NotFoundException(String.format("Пользователь с id = %d  - не найден", updatedUser.getId()));

    }

    @Override
    public void deleteUser(int userId) {
        if (!users.containsKey(userId)) {
            throw new NotFoundException(String.format("Пользователь с id = %d  - не найден", userId));
        }
        users.remove(userId);
    }

    @Override
    public Optional<User> findUserById(int userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public List<User> findAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void addFriendRequest(int userId, int friendId) {

    }

    @Override
    public void deleteFriend(int userId, int friendId) {
    }

    @Override
    public List<UserFriendDto> findFriendsByUserId(int userId) {
        return List.of();
    }

    private int getNextId() {
        int currentMaxId = users.keySet().stream().mapToInt(id -> id).max().orElse(0);
        return ++currentMaxId;
    }

}
