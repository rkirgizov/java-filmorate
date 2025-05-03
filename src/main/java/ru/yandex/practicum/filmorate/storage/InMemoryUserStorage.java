
package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

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
    public Collection<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User findUserById(Long userId) {
        return users.get(userId);
    }

    @Override
    public void deleteUser(Long userId) {
        users.remove(userId);
    }

    private long getNextId() {
        long currentMaxId = users.keySet().stream().mapToLong(id -> id).max().orElse(0);
        return ++currentMaxId;
    }

}
