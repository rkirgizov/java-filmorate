package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Getter
@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    protected final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        User validUser = validateUser(user);
        validUser.setId(getNextId());
        users.put(validUser.getId(), validUser);
        log.info("Пользователь с id = {}  - добавлен", validUser.getId());
        return validUser;
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        if (newUser.getId() == null) {
            throw new ValidationException("Для обновления пользователя необходимо указать id");
        }
        if (users.containsKey(newUser.getId())) {
            User validUser = validateUser(newUser);
            User oldUser = users.get(validUser.getId());
            oldUser.setEmail(validUser.getEmail());
            oldUser.setName(validUser.getName());
            oldUser.setLogin(validUser.getLogin());
            oldUser.setBirthday(validUser.getBirthday());
            log.info("Пользователь \"{}\" с id = {}  - обновлен", validUser.getName(), validUser.getId());
            return oldUser;
        }
        throw new NotFoundException(String.format("Пользователь с id = %d  - не найден", newUser.getId()));
    }

    private User validateUser(User user) {
        for (User value : users.values()) {
            if (user.getEmail().equals(value.getEmail())) {
                throw new ValidationException("Пользователь с таким email уже существует");
            }
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Пустое имя пользователя заменено на логин");
        }
        return user;
    }

        private long getNextId() {
        long currentMaxId = users.keySet().stream().mapToLong(id -> id).max().orElse(0);
        return ++currentMaxId;
    }

}