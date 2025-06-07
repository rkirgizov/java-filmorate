package ru.yandex.practicum.filmorate.validation;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.dto.ReviewRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Optional;

@Slf4j
@Data
public final class ReviewValidator {

    public static void validateReviewForCreate(ReviewRequest reviewRequest, UserStorage userStorage, FilmStorage filmStorage) {
        if (hasNoContent(reviewRequest)) {
            throw new ValidationException("Отзыв не может быть пустым");
        }

        if (hasNoIsPositive(reviewRequest)) {
            throw new ValidationException("Поле isPositive должно быть заполнено");
        }

        if (hasNoUserId(reviewRequest)) {
            throw new ValidationException("Поле userId должно быть заполнено");
        }
        validateUserId(reviewRequest.getUserId(), userStorage);

        if (hasNoFilmId(reviewRequest)) {
            throw new ValidationException("Поле filmId должно быть заполнено");
        }
        validateFilmId(reviewRequest.getFilmId(), filmStorage);

    }

    public static ReviewRequest validateReviewRequestForUpdate(Review review, ReviewRequest reviewRequest) {
        if (hasNoContent(reviewRequest)) {
            reviewRequest.setContent(review.getContent());
        }

        if (hasNoIsPositive(reviewRequest)) {
            reviewRequest.setIsPositive(review.getIsPositive());
        }

        // Возможно обновление только контента и положительности
        // Контролируем, что userId, filmId, useful не изменятся
        reviewRequest.setUserId(review.getUserId());
        reviewRequest.setFilmId(review.getFilmId());

        return reviewRequest;
    }

    /**
     * Проверка наличия параметров в реквесте
     */

    public static boolean hasNoContent(ReviewRequest request) {
        return request.getContent() == null || request.getContent().isBlank();
    }

    public static boolean hasNoIsPositive(ReviewRequest request) {
        return request.getIsPositive() == null;
    }

    public static boolean hasNoUserId(ReviewRequest request) {
        return request.getUserId() == null;
    }

    public static boolean hasNoFilmId(ReviewRequest request) {
        return request.getFilmId() == null;
    }

    /**
     * Валидация параметров отзыва
     */

    public static void validateNewRating(boolean isLike, int reviewId, int userId, ReviewStorage reviewStorage, UserStorage userStorage) {
        validateReviewId(reviewId, reviewStorage);
        validateUserId(userId, userStorage);
        Optional<Boolean> userReviewRating = reviewStorage.getUserReviewRating(reviewId, userId);
        if (userReviewRating.isPresent() && userReviewRating.get() == isLike) {
            throw new ValidationException(String.format("Пользователь с id: %s уже поставил оценку isLike: %s отзыву с id: %s", userId, isLike, reviewId));
        }
    }

    public static void validateReviewId(int reviewId, ReviewStorage reviewStorage) {
        reviewStorage.findReviewById(reviewId)
                .orElseThrow(() -> new NotFoundException(String.format("Отзыв с id: %s не найден", reviewId)));
    }

    public static void validateUserId(int userId, UserStorage userStorage) {
        userStorage.findUserById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id: %s не найден", userId)));
    }

    public static void validateFilmId(int filmId, FilmStorage filmStorage) {
        filmStorage.findFilmById(filmId)
                .orElseThrow(() -> new NotFoundException(String.format("Фильм с id: %s не найден", filmId)));
    }

}
