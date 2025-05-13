package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.dto.UserFriendDto;
import ru.yandex.practicum.filmorate.dto.UserRequest;
import ru.yandex.practicum.filmorate.exception.NonCriticalException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.validation.UserValidator;

import java.util.*;

//@RequiredArgsConstructor Убрал, так как Qualifier не работает с @RequiredArgsConstructor
@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;

    public UserService(@Qualifier("UserStorageDbImpl") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public UserDto findUserById(Integer userId) {
        return userStorage.findUserById(userId)
                .map(UserMapper::mapToUserDto)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id: %s не найден", userId)));
    }

    public List<UserDto> findAllUsers() {
        return userStorage.findAllUsers().stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    public UserDto createUser(UserRequest userRequest) {
        validateUserForCreate(userRequest);
        for (User value : userStorage.findAllUsers()) {
            if (userRequest.getLogin().equals(value.getLogin()) || userRequest.getEmail().equals(value.getEmail())) {
                throw new ValidationException("Пользователь с таким login или email уже существует");
            }
        }
        User user = UserMapper.mapToUser(userRequest);
        user = userStorage.createUser(user);

        return UserMapper.mapToUserDto(user);
    }

    public UserDto updateUser(int userId, UserRequest userRequest) {
        User userForUpdate = userStorage.findUserById(userId)
                .map(user -> UserMapper.mapUserFieldsForUpdate(user, userRequest))
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id: %s не найден", userId)));
        for (User value : userStorage.findAllUsers()) {
            if (userRequest.hasLogin()) {
                if (!userForUpdate.getId().equals(value.getId()) && userForUpdate.getLogin().equals(value.getLogin())) {
                    throw new ValidationException("Обновлённый логин совпадает с логином другого пользователя");
                }
            }
            if (userRequest.hasEmail()) {
                if (!userForUpdate.getId().equals(value.getId()) && userForUpdate.getEmail().equals(value.getEmail())) {
                    throw new ValidationException("Обновлённый email совпадает с email другого пользователя");
                }
            }
        }
        User userUpdated = userStorage.updateUser(userForUpdate);

        return UserMapper.mapToUserDto(userUpdated);
    }

    public void removeUser(int userId) {
        userStorage.findUserById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id: %s не найден", userId)));
        userStorage.deleteUser(userId);
    }

    public void addFriendRequest(int userId, int userFriendId) {
        for (UserFriendDto value : userStorage.findFriendsByUserId(userId)) {
            if (userFriendId == value.getId()) {
                throw new ValidationException(String.format("Пользователь с id: %s уже является другом пользователя с id: %s", userFriendId, userId));
            }
        }
        if (userId == userFriendId) {
            throw new ValidationException("Невозможно добавить пользователя в друзья самому себе");
        }
        if (userStorage.findUserById(userId).isEmpty() || userStorage.findUserById(userFriendId).isEmpty()) {
            throw new NotFoundException("Один из пользователей не найден");
        }
        userStorage.addFriendRequest(userId, userFriendId);

        log.debug("Пользователь {} добавил в друзья пользователя {}", userId, userFriendId);
    }

    public void removeFromFriends(int userId, int userFriendId) {
        if (userStorage.findUserById(userId).isEmpty() || userStorage.findUserById(userFriendId).isEmpty()) {
            throw new NotFoundException("Один из пользователей не найден");
        }
        if (userStorage.findFriendsByUserId(userId).isEmpty()) {
            throw new NonCriticalException("Нет друзей для удаления");
        }
        userStorage.deleteFriend(userId, userFriendId);
    }

    public List<UserFriendDto> findFriendsByUserId(int userId) {
        if (userStorage.findUserById(userId).isEmpty()) {
            throw new NotFoundException(String.format("Пользователь с id: %s не найден", userId));
        }
        return userStorage.findFriendsByUserId(userId).stream()
                .toList();
    }

    public List<UserFriendDto> findCommonFriends(int userId, int otherUserId) {
        if (userStorage.findUserById(userId).isEmpty() || userStorage.findUserById(otherUserId).isEmpty()) {
            throw new NotFoundException("Один из пользователей не найден");
        }
        List<UserFriendDto> friends1 = userStorage.findFriendsByUserId(userId);
        List<UserFriendDto> friends2 = userStorage.findFriendsByUserId(otherUserId);
        friends1.removeIf(value -> !friends2.contains(value));
        if (friends1.isEmpty()) {
            throw new NotFoundException("Нет общих друзей");
        }

        return friends1;
    }

    private void validateUserForCreate(UserRequest userRequest) {
        if (!userRequest.hasEmail()) {
            throw new ValidationException("Электронная почта не заполнена");
        }
        UserValidator.validateEmail(userRequest.getEmail());

        if (!userRequest.hasLogin()) {
            throw new ValidationException("Логин не заполнен");
        }
        UserValidator.validateLogin(userRequest.getLogin());

        if (!userRequest.hasBirthday()) {
            throw new ValidationException("Дата рождения не заполнена");
        }
        UserValidator.validateBirthday(userRequest.getBirthday());

        if (!userRequest.hasName()) {
            userRequest.setName(userRequest.getLogin());
            log.debug("Пустое имя пользователя заменено на логин");
        }
    }

}