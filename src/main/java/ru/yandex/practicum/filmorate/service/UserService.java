package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.*;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;

    public User findUserById(Long userId) {
        User user = userStorage.findUserById(userId);
        if (user == null) {
            throw new NotFoundException(String.format("Пользователь с id: %s не найден", userId));
        }
        return user;
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User createUser(User user) {
        validateUser(user);
        for (User value : userStorage.findAll()) {
            if (user.getEmail().equals(value.getEmail())) {
                throw new ValidationException("Пользователь с таким email уже существует");
            }
        }
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        validateUser(user);
        return userStorage.updateUser(user);
    }

    public void addToFriends(Long userId, Long userFriendId) {
        if (userId.equals(userFriendId)) {
            throw new ValidationException(String.format("Пользователь с id %s не может являться другом самого себя", userId));
        }
        User user = findUserById(userId);
        User userFriend = findUserById(userFriendId);
        user.getFriends().add(userFriendId);
        userFriend.getFriends().add(userId);
    }

    public void removeFromFriends(Long userId, Long userFriendId) {
        User user = findUserById(userId);
        User userFriend = findUserById(userFriendId);
        user.getFriends().remove(userFriendId);
        userFriend.getFriends().remove(userId);
    }

    public List<User> getFriendsList(Long userId) {
        User user = findUserById(userId);
        Set<Long> userFriendsId = user.getFriends();
        return userFriendsId.stream()
                .map(userStorage::findUserById)
                .filter(Objects::nonNull)
                .toList();
    }

    public List<User> listCommonFriends(Long userId, Long otherUserId) {
        User user = findUserById(userId);
        User otherUser = findUserById(otherUserId);
        Set<Long> userFriendsId = user.getFriends();
        Set<Long> otherUserFriendsId = otherUser.getFriends();
        Set<Long> commonFriendsId = new HashSet<>(userFriendsId);
        commonFriendsId.retainAll(otherUserFriendsId);
        if (userFriendsId.isEmpty()) {
            throw new NotFoundException(String.format("У пользователей с id: %s и %s нет общих друзей", userId, otherUserId));
        }
        return commonFriendsId.stream()
                .map(userStorage::findUserById)
                .filter(Objects::nonNull)
                .toList();
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ValidationException("Электронная почта не заполнена");
        }
        if (!user.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new ValidationException("Электронная почта введена некорректно");
        }
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            throw new ValidationException("Логин не заполнен");
        }
        if (user.getLogin().matches(".*\\s.*")) {
            throw new ValidationException("Логин не может содержать пробелы");
        }
        if (user.getBirthday() == null) {
            throw new ValidationException("Дата рождения не заполнена");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Пустое имя пользователя заменено на логин");
        }
    }

}