package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public User findById(@PathVariable Long id) {
        return userService.findUserById(id);
    }

    @GetMapping
    public Collection<User> findAll() {
        return userService.findAll();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        return userService.createUser(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        return userService.updateUser(user);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addToFriends(@PathVariable("id") Long id, @PathVariable("friendId") Long friendId) {
        userService.addToFriends(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFromFriends(@PathVariable("id") Long id, @PathVariable("friendId") Long friendId) {
        userService.removeFromFriends(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriendsList(@PathVariable("id") Long id) {
        return userService.getFriendsList(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> listCommonFriends(@PathVariable("id") Long id, @PathVariable("otherId") Long otherId) {
        return userService.listCommonFriends(id, otherId);
    }

}