package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody UserRequest userRequest) {
        log.debug("Получен запрос POST /users для создания пользователя {}", userRequest);
        return userService.createUser(userRequest);
    }

    @GetMapping("/{id}")
    public UserDto findUserById(@PathVariable Integer id) {
        log.debug("Получен запрос GET /users/{} для получения пользователя", id);
        return userService.findUserById(id);
    }

    @GetMapping
    public List<UserDto> findAllUsers() {
        log.debug("Получен запрос GET /users для получения списка пользователей");
        return userService.findAllUsers();
    }

    @PutMapping
    public UserDto updateUser(@Valid @RequestBody UserRequestUpdate request) {
        log.debug("Получен запрос PUT /users для обновления пользователя {}", request);
        Integer userId = request.getId();
        return userService.updateUser(userId, request);
    }

    @DeleteMapping("/{userId}")
    public void removeUser(@PathVariable Integer userId) {
        log.debug("Получен запрос DELETE /users/{} для удаления пользователя", userId);
        userService.removeUser(userId);
    }

    @PutMapping("/{userid}/friends/{friendId}")
    public void addFriendRequest(@PathVariable("userid") Integer userid, @PathVariable("friendId") Integer friendId) {
        log.debug("Получен запрос PUT /users/{}/friends/{} для добавления пользователя в друзья", userid, friendId);
        userService.addFriendRequest(userid, friendId);
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    public void removeFromFriends(@PathVariable("userId") Integer userId, @PathVariable("friendId") Integer friendId) {
        log.debug("Получен запрос DELETE /users/{}/friends/{} для удаления пользователя из друзей", userId, friendId);
        userService.removeFromFriends(userId, friendId);
    }

    @GetMapping("/{userId}/friends")
    public List<UserFriendDto> findFriendsByUserId(@PathVariable Integer userId) {
        log.debug("Получен запрос GET /users/{}/friends для получения списка друзей пользователя", userId);
        return userService.findFriendsByUserId(userId);
    }

    @GetMapping("/{userId}/friends/common/{otherUserId}")
    public List<UserFriendDto> findCommonFriends(@PathVariable("userId") Integer userId, @PathVariable("otherUserId") Integer otherUserId) {
        log.debug("Получен запрос GET /users/{}/friends/common/{} для получения списка общих друзей", userId, otherUserId);
        return userService.findCommonFriends(userId, otherUserId);
    }

    @GetMapping("/{userId}/feed")
    public List<UserEventDto> getFeed(@PathVariable Integer userId) {
        log.debug("Получен запрос GET /users/{}/feed для получения ленты событий пользователя", userId);
        return userService.findEventsByUserId(userId);
    }

    @GetMapping("/{id}/recommendations")
    public List<FilmDto> findRecommendedFilms(@PathVariable Integer id) {
        log.debug("Получен запрос GET /users/{}/recommendations для получения рекомендаций фильмов", id);
        return userService.findRecommendedFilms(id);
    }
}