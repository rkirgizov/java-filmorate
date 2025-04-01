package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserTest {

    @Autowired
    private Validator validator;

    private User user;

    @BeforeEach
    public void setUp() {
        user = new User();
    }

    @Test
    void validationEmailWorkCorrectly() {
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        // email is null
        user.setEmail(null);
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(),"Ожидается ошибка валидации из-за null в email");
        // email is blank
        user.setEmail("");
        violations = validator.validate(user);
        assertFalse(violations.isEmpty(),"Ожидается ошибка валидации из-за пустой строки в email");
        // email is invalid
        user.setEmail("InvalidEmail");
        violations = validator.validate(user);
        assertFalse(violations.isEmpty(),"Ожидается ошибка валидации из-за невалидного email");
    }

    @Test
    void validationLoginWorkCorrectly() {
        user.setEmail("email@example.com");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        // login is null
        user.setLogin(null);
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(),"Ожидается ошибка валидации из-за null в login");
        // login is blank
        user.setLogin("");
        violations = validator.validate(user);
        assertFalse(violations.isEmpty(),"Ожидается ошибка валидации из-за пустой строки в login");
        // login with spaces
        user.setLogin("invalid Login");
        violations = validator.validate(user);
        assertFalse(violations.isEmpty(),"Ожидается ошибка валидации из-за логина с пробелами");
    }

    @Test
    void validationBirthdayWorkCorrectly() {
        user.setEmail("email@example.com");
        user.setLogin("login");
        // birthday is null
        user.setBirthday(null);
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(),"Ожидается ошибка валидации из-за null в дате рождения");
        // birthday is in the future
        user.setBirthday(LocalDate.now().plusDays(1));
        violations = validator.validate(user);
        assertFalse(violations.isEmpty(),"Ожидается ошибка валидации из-за даты рождения в будущем");
    }

    @Test
    void validationPassCorrectly() {
        user.setEmail("email@example.com");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(),"Ожидается отсутствие ошибок валидации");
    }
}