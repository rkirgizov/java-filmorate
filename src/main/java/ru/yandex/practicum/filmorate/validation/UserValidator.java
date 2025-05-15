package ru.yandex.practicum.filmorate.validation;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.dto.UserRequest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;

@Slf4j
@Data
public final class UserValidator {

    public static void validateUserForCreate(UserRequest userRequest) {
        if (hasNoLogin(userRequest)) {
            throw new ValidationException("Логин не заполнен");
        }
        validateLogin(userRequest.getLogin());

        if (hasNoEmail(userRequest)) {
            throw new ValidationException("Электронная почта не заполнена");
        }
        validateEmail(userRequest.getEmail());

        if (hasNoBirthday(userRequest)) {
            throw new ValidationException("Дата рождения не заполнена");
        }
        validateBirthday(userRequest.getBirthday());

        if (hasNoName(userRequest)) {
            userRequest.setName(userRequest.getLogin());
            log.debug("Пустое имя пользователя заменено на логин");
        }
    }

    public static UserRequest validateUserRequestForUpdate(User user, UserRequest userRequest, UserStorage userStorage) {
        if (hasNoLogin(userRequest)) {
            userRequest.setLogin(user.getLogin());
        } else {
            validateLogin(userRequest.getLogin());
        }

        if (hasNoEmail(userRequest)) {
            userRequest.setEmail(user.getEmail());
        } else {
            validateEmail(userRequest.getEmail());
        }

        if (hasNoBirthday(userRequest)) {
            userRequest.setBirthday(user.getBirthday());
        } else {
            validateBirthday(userRequest.getBirthday());
        }

        if (hasNoName(userRequest)) {
            userRequest.setName(user.getName());
        }

        for (User value : userStorage.findAllUsers()) {
            if (!hasNoLogin(userRequest)) {
                if (!user.getId().equals(value.getId()) && user.getLogin().equals(value.getLogin())) {
                    throw new ValidationException("Обновлённый логин совпадает с логином другого пользователя");
                }
            }
            if (!hasNoEmail(userRequest)) {
                if (!user.getId().equals(value.getId()) && user.getEmail().equals(value.getEmail())) {
                    throw new ValidationException("Обновлённый email совпадает с email другого пользователя");
                }
            }
        }

        return userRequest;
    }

    /**
     * Проверка наличия параметров в реквесте
     */

    public static boolean hasNoLogin(UserRequest request) {
        return request.getLogin() == null || request.getLogin().isBlank();
    }

    public static boolean hasNoEmail(UserRequest request) {
        return request.getEmail() == null || request.getEmail().isBlank();
    }

    public static boolean hasNoName(UserRequest request) {
        return request.getName() == null || request.getName().isBlank();
    }

    public static boolean hasNoBirthday(UserRequest request) {
        return request.getBirthday() == null;
    }

    /**
     * Валидация параметров пользователя
     */

    public static void validateLogin(String login) {
        if (login.matches(".*\\s.*")) {
            throw new ValidationException("Логин не может содержать пробелы");
        }
    }

    public static void validateEmail(String email) {
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new ValidationException("Электронная почта введена некорректно");
        }
    }

    public static void validateBirthday(LocalDate birthday) {
        if (birthday.isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}
