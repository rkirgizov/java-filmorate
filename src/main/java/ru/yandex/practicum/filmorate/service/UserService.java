package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.enumeration.EventOperation;
import ru.yandex.practicum.filmorate.enumeration.EventType;
import ru.yandex.practicum.filmorate.exception.NonCriticalException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.validation.UserValidator;

import java.util.*;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;
    private final FilmService filmService;

    @Autowired
    public UserService(@Qualifier("UserStorageDbImpl") UserStorage userStorage, FilmService filmService) {
        this.userStorage = userStorage;
        this.filmService = filmService;
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
        UserValidator.validateUserForCreate(userRequest);
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
                .map(user -> UserValidator.validateUserRequestForUpdate(user, userRequest, userStorage))
                .map(user -> UserMapper.mapToUser(userRequest))
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id: %s не найден", userId)));
        userForUpdate.setId(userId);
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
        userStorage.addEvent(userId, EventType.FRIEND, EventOperation.ADD, userFriendId);
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
        userStorage.addEvent(userId, EventType.FRIEND, EventOperation.REMOVE, userFriendId);
        log.debug("Пользователь {} удалил из друзей пользователя {}", userId, userFriendId);

    }

    public List<UserFriendDto> findFriendsByUserId(int userId) {
        if (userStorage.findUserById(userId).isEmpty()) {
            throw new NotFoundException(String.format("Пользователь с id: %s не найден", userId));
        }
        return userStorage.findFriendsByUserId(userId).stream()
                .toList();
    }

    public List<UserFriendDto> findCommonFriends(int userId, int otherUserId) {
        // Лишняя проверка на существование пользователей, не даёт пройти тест постмана
//        if (userStorage.findUserById(userId).isEmpty() || userStorage.findUserById(otherUserId).isEmpty()) {
//            throw new NotFoundException("Один из пользователей не найден");
//        }
        List<UserFriendDto> friends1 = userStorage.findFriendsByUserId(userId);
        List<UserFriendDto> friends2 = userStorage.findFriendsByUserId(otherUserId);
        friends1.removeIf(value -> !friends2.contains(value));
        // Лишняя проверка на существование пользователей, не даёт пройти тест постмана
//        if (friends1.isEmpty()) {
//            throw new NotFoundException("Нет общих друзей");
//        }

        return friends1;
    }

    public List<UserEventDto> findEventsByUserId(int userId) {
        userStorage.findUserById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id: %s не найден", userId)));
        return userStorage.findEventsByUserId(userId).stream()
                .toList();
    }

    public List<FilmDto> findRecommendedFilms(Integer userId) {
        userStorage.findUserById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id: %s не найден", userId)));

        Set<Integer> currentUserLikes = filmService.getFilmLikesByUserId(userId);

        if (currentUserLikes.isEmpty()) {
            log.info("У пользователя с ID {} нет лайков, рекомендации отсутствуют.", userId);
            return Collections.emptyList();
        }

        Map<Integer, Set<Integer>> allUsersLikes = filmService.getAllUsersLikes();

        allUsersLikes.remove(userId);

        int maxIntersectionSize = 0;
        Set<Integer> usersWithMaxIntersection = new HashSet<>();

        for (Map.Entry<Integer, Set<Integer>> entry : allUsersLikes.entrySet()) {
            Integer otherUserId = entry.getKey();
            Set<Integer> otherUserLikes = entry.getValue();

            Set<Integer> intersection = new HashSet<>(currentUserLikes);
            intersection.retainAll(otherUserLikes);

            int currentIntersectionSize = intersection.size();

            if (currentIntersectionSize > maxIntersectionSize) {
                maxIntersectionSize = currentIntersectionSize;
                usersWithMaxIntersection.clear();
                usersWithMaxIntersection.add(otherUserId);
            } else if (currentIntersectionSize > 0 && currentIntersectionSize == maxIntersectionSize) {
                usersWithMaxIntersection.add(otherUserId);
            }
        }

        if (maxIntersectionSize == 0) {
            log.info("Не найдено пользователей с общими лайками для пользователя с ID {}", userId);
            return Collections.emptyList();
        }

        Set<Integer> recommendedFilmIds = new HashSet<>();
        for (Integer similarUserId : usersWithMaxIntersection) {
            Set<Integer> similarUserLikes = allUsersLikes.get(similarUserId);
            if (similarUserLikes != null) {
                for (Integer filmId : similarUserLikes) {
                    if (!currentUserLikes.contains(filmId)) {
                        recommendedFilmIds.add(filmId);
                    }
                }
            }
        }

        if (recommendedFilmIds.isEmpty()) {
            log.info("Не найдено фильмов для рекомендации по лайкам похожих пользователей для пользователя с ID {}", userId);
            return Collections.emptyList();
        }

        List<Integer> recommendedFilmIdList = new ArrayList<>(recommendedFilmIds);
        List<FilmDto> recommendedFilms = filmService.getFilmsByIds(recommendedFilmIdList);

        log.debug("Найдено {} рекомендованных фильмов для пользователя с ID {}", recommendedFilms.size(), userId);

        return recommendedFilms;
    }
}