package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.dto.UserFriendDto;
import ru.yandex.practicum.filmorate.dto.UserRequest;
import ru.yandex.practicum.filmorate.dto.UserRequestUpdate;
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
        return userService.createUser(userRequest);
    }

    @GetMapping("/{id}")
    public UserDto findUserById(@PathVariable Integer id) {
        return userService.findUserById(id);
    }

    @GetMapping
    public List<UserDto> findAllUsers() {
        return userService.findAllUsers();
    }

    @PutMapping
    public UserDto updateUser(@Valid @RequestBody UserRequestUpdate request) {
        Integer userId = request.getId();
        return userService.updateUser(userId, request);
    }

    @DeleteMapping("/{userId}")
    public void removeUser(@PathVariable Integer userId) {
        userService.removeUser(userId);
    }

    @PutMapping("/{userid}/friends/{friendId}")
    public void addFriendRequest(@PathVariable("userid") Integer userid, @PathVariable("friendId") Integer friendId) {
        userService.addFriendRequest(userid, friendId);
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    public void removeFromFriends(@PathVariable("userId") Integer userId, @PathVariable("friendId") Integer friendId) {
        userService.removeFromFriends(userId, friendId);
    }

    @GetMapping("/{userId}/friends")
    public List<UserFriendDto> findFriendsByUserId(@PathVariable Integer userId) {
        return userService.findFriendsByUserId(userId);
    }

    @GetMapping("/{userId}/friends/common/{otherUserId}")
    public List<UserFriendDto> findCommonFriends(@PathVariable("userId") Integer userId, @PathVariable("otherUserId") Integer otherUserId) {
        return userService.findCommonFriends(userId, otherUserId);
    }

}