package ru.yandex.practicum.filmorate.validation;

import lombok.Data;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;

@Data
public final class UserValidator {

    public static void validateEmail(String email) {
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new ValidationException("Электронная почта введена некорректно");
        }
    }

    public static void validateLogin(String login) {
        if (login.matches(".*\\s.*")) {
            throw new ValidationException("Логин не может содержать пробелы");
        }
    }

    public static void validateBirthday(LocalDate birthday) {
        if (birthday.isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}
