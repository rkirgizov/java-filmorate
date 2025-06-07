package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.ReviewRequest;
import ru.yandex.practicum.filmorate.enumeration.EventOperation;
import ru.yandex.practicum.filmorate.enumeration.EventType;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.validation.ReviewValidator;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public ReviewService(@Qualifier("ReviewStorageDbImpl") ReviewStorage reviewStorage,
                         @Qualifier("UserStorageDbImpl") UserStorage userStorage,
                         @Qualifier("FilmStorageDbImpl") FilmStorage filmStorage) {
        this.reviewStorage = reviewStorage;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    private Review getReviewOrThrow(int reviewId) {
        return reviewStorage.findReviewById(reviewId)
                .orElseThrow(() -> new NotFoundException(String.format("Отзыв с id = %d не найден", reviewId)));
    }

    public ReviewDto findReviewById(int reviewId) {
        log.info("Получение отзыва по id: {}", reviewId);
        Review review = getReviewOrThrow(reviewId);
        return ReviewMapper.mapToReviewDto(review);
    }

    public List<ReviewDto> findAll(Integer filmId, Integer count) {
        log.info("Получение списка отзывов. filmId: {}, count: {}", filmId, count);
        List<Review> reviews;
        if (filmId != null) {
            reviews = reviewStorage.findAllReviews(filmId, count);
        } else {
            reviews = reviewStorage.findAllReviews(count);
        }
        return reviews.stream()
                .map(ReviewMapper::mapToReviewDto)
                .toList();
    }

    public ReviewDto createReview(ReviewRequest reviewRequest) {
        log.info("Создание отзыва: {}", reviewRequest);
        ReviewValidator.validateReviewForCreate(reviewRequest, userStorage, filmStorage);
        for (Review value : reviewStorage.findAllReviews()) {
            if (reviewRequest.getUserId().equals(value.getUserId()) && reviewRequest.getFilmId().equals(value.getFilmId())) {
                throw new ValidationException(String.format("Пользователь с id: %s уже оставлял отзыв на фильм с id: %s", reviewRequest.getUserId(), reviewRequest.getFilmId()));
            }
        }
        Review review = ReviewMapper.mapToReview(reviewRequest);
        review = reviewStorage.createReview(review);
        userStorage.addEvent(reviewRequest.getUserId(), EventType.REVIEW, EventOperation.ADD, review.getId());
        log.info("Отзыв с id = {} создан", review.getId());

        return ReviewMapper.mapToReviewDto(review);
    }

    public ReviewDto updateReview(int reviewId, ReviewRequest reviewRequest) {
        log.info("Обновление отзыва с id {}: {}", reviewId, reviewRequest);
        Review existingReview = getReviewOrThrow(reviewId);
        ReviewRequest validatedReviewRequestForUpdate = ReviewValidator.validateReviewRequestForUpdate(existingReview, reviewRequest);

        Review reviewToPersist = ReviewMapper.mapToReview(validatedReviewRequestForUpdate);
        reviewToPersist.setId(reviewId);
        Review reviewUpdated = reviewStorage.updateReview(reviewToPersist);
        userStorage.addEvent(reviewUpdated.getUserId(), EventType.REVIEW, EventOperation.UPDATE, reviewUpdated.getId());
        log.info("Отзыв с id = {} обновлен", reviewId);

        return ReviewMapper.mapToReviewDto(reviewUpdated);
    }

    public void removeReview(int reviewId) {
        log.info("Удаление отзыва с id: {}", reviewId);
        Review reviewForRemove = getReviewOrThrow(reviewId);
        reviewStorage.removeReview(reviewId);
        userStorage.addEvent(reviewForRemove.getUserId(), EventType.REVIEW, EventOperation.REMOVE, reviewForRemove.getId());
        log.info("Отзыв с id = {} удален", reviewId);
    }

    public void addLike(int reviewId, int userId) {
        log.info("Пользователь {} ставит лайк отзыву {}", userId, reviewId);
        getReviewOrThrow(reviewId);
        ReviewValidator.validateNewRating(true, reviewId, userId, reviewStorage, userStorage);

        Optional<Boolean> userReviewRating = reviewStorage.getUserReviewRating(reviewId, userId);

        if (userReviewRating.isPresent() && !userReviewRating.get()) {
            reviewStorage.removeDislike(reviewId, userId);
        }
        reviewStorage.addLike(reviewId, userId);
        log.info("Лайк пользователя {} добавлен к отзыву {}", userId, reviewId);
    }

    public void removeLike(int reviewId, int userId) {
        log.info("Пользователь {} удаляет лайк с отзыва {}", userId, reviewId);
        getReviewOrThrow(reviewId);

        Optional<Boolean> userReviewRating = reviewStorage.getUserReviewRating(reviewId, userId);

        if (userReviewRating.isEmpty()) {
            throw new NotFoundException(String.format("Не найдена оценка пользователя с id: %s на отзыв с id: %s", userId, reviewId));
        }
        if (userReviewRating.get()) {
            reviewStorage.removeLike(reviewId, userId);
            log.info("Лайк пользователя {} удален с отзыва {}", userId, reviewId);
        } else {
            throw new NotFoundException(String.format("Пользователь с id: %s не ставил лайк на отзыв с id: %s", userId, reviewId));
        }
    }

    public void addDislike(int reviewId, int userId) {
        log.info("Пользователь {} ставит дизлайк отзыву {}", userId, reviewId);
        getReviewOrThrow(reviewId);
        ReviewValidator.validateNewRating(false, reviewId, userId, reviewStorage, userStorage);

        Optional<Boolean> userReviewRating = reviewStorage.getUserReviewRating(reviewId, userId);
        if (userReviewRating.isPresent() && userReviewRating.get()) {
            reviewStorage.removeLike(reviewId, userId);
        }
        reviewStorage.addDislike(reviewId, userId);
        log.info("Дизлайк пользователя {} добавлен к отзыву {}", userId, reviewId);
    }

    public void removeDislike(int reviewId, int userId) {
        log.info("Пользователь {} удаляет дизлайк с отзыва {}", userId, reviewId);
        getReviewOrThrow(reviewId);

        Optional<Boolean> userReviewRating = reviewStorage.getUserReviewRating(reviewId, userId);
        if (userReviewRating.isEmpty() || userReviewRating.get()) {
            throw new NotFoundException(String.format("Не найден дизлайк пользователя с id: %s на отзыв с id: %s", userId, reviewId));
        }
        reviewStorage.removeDislike(reviewId, userId);
        log.info("Дизлайк пользователя {} удален с отзыва {}", userId, reviewId);
    }
}