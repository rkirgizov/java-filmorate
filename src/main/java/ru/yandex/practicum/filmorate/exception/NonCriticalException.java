package ru.yandex.practicum.filmorate.exception;

public class NonCriticalException extends RuntimeException {
    public NonCriticalException(String message) {
        super(message);
    }
}
