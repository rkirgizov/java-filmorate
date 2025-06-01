package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.*;
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

    public ReviewDto findReviewById(int reviewId) {
        return reviewStorage.findReviewById(reviewId)
                .map(review -> ReviewMapper.mapToReviewDto(review))
                .orElseThrow(() -> new NotFoundException(String.format("Отзыв с id = %d не найден", reviewId)));
    }

    public List<ReviewDto> findAll(Integer filmId, Integer count) {
        if (filmId != null) {
            return reviewStorage.findAllReviews(filmId, count).stream()
                    .map(review -> ReviewMapper.mapToReviewDto(review))
                    .toList();
        }
        return reviewStorage.findAllReviews(count).stream()
                .map(review -> ReviewMapper.mapToReviewDto(review))
                .toList();
    }

    public ReviewDto createReview(ReviewRequest reviewRequest) {
        ReviewValidator.validateReviewForCreate(reviewRequest, userStorage, filmStorage);
        for (Review value : reviewStorage.findAllReviews()) {
            if (reviewRequest.getUserId().equals(value.getUserId()) && reviewRequest.getFilmId().equals(value.getFilmId())) {
                throw new ValidationException(String.format("Пользователь с id: %s уже оставлял отзыв на фильм с id: %s", reviewRequest.getUserId(), reviewRequest.getFilmId()));
            }
        }
        Review review = ReviewMapper.mapToReview(reviewRequest);
        review = reviewStorage.createReview(review);

        return ReviewMapper.mapToReviewDto(review);
    }

    public ReviewDto updateReview(int reviewId, ReviewRequest reviewRequest) {
        Review existingReview = reviewStorage.findReviewById(reviewId)
                .orElseThrow(() -> new NotFoundException(String.format("Отзыв с id: %s не найден", reviewId)));
        ReviewRequest validatedReviewRequestForUpdate = ReviewValidator.validateReviewRequestForUpdate(existingReview, reviewRequest);

        Review reviewToPersist = ReviewMapper.mapToReview(validatedReviewRequestForUpdate);
        reviewToPersist.setId(reviewId);
        Review reviewUpdated = reviewStorage.updateReview(reviewToPersist);

        return ReviewMapper.mapToReviewDto(reviewUpdated);
    }

    public void removeReview(int reviewId) {
        reviewStorage.findReviewById(reviewId)
                .orElseThrow(() -> new NotFoundException(String.format("Отзыв с id: %s не найден", reviewId)));
        reviewStorage.removeReview(reviewId);
    }

    public void addLike(int reviewId, int userId) {
        ReviewValidator.validateNewRating(true, reviewId, userId, reviewStorage, userStorage);
        Optional<Boolean> userReviewRating = reviewStorage.getUserReviewRating(reviewId, userId);
        if (userReviewRating.isPresent() && !userReviewRating.get()) {
            reviewStorage.removeDislike(reviewId, userId);
        }
        reviewStorage.addLike(reviewId, userId);
    }

    public void removeLike(int reviewId, int userId) {
        Optional<Boolean> userReviewRating = reviewStorage.getUserReviewRating(reviewId, userId);
        if (userReviewRating.isEmpty()) {
            throw new NotFoundException(String.format("Не найдена оценка пользователя с id: %s на отзыв с id: %s", userId, reviewId));
        }
        if (userReviewRating.get()) {
            reviewStorage.removeLike(reviewId, userId);
        } else {
            reviewStorage.removeDislike(reviewId, userId);
        }
    }

    public void addDislike(int reviewId, int userId) {
        ReviewValidator.validateNewRating(false, reviewId, userId, reviewStorage, userStorage);
        Optional<Boolean> userReviewRating = reviewStorage.getUserReviewRating(reviewId, userId);
        if (userReviewRating.isPresent() && userReviewRating.get()) {
            reviewStorage.removeLike(reviewId, userId);
        }
        reviewStorage.addDislike(reviewId, userId);
    }

    public void removeDislike(int reviewId, int userId) {
        Optional<Boolean> userReviewRating = reviewStorage.getUserReviewRating(reviewId, userId);
        if (userReviewRating.isEmpty() || userReviewRating.get()) {
            throw new NotFoundException(String.format("Не найден дизлайк пользователя с id: %s на отзыв с id: %s", userId, reviewId));
        }
        reviewStorage.removeDislike(reviewId, userId);
    }

}