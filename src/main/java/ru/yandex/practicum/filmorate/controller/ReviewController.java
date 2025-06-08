package ru.yandex.practicum.filmorate.controller;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping("/{id}")
    public ReviewDto findReviewById(@PathVariable("id") Integer id) {
        log.debug("Получен запрос GET /reviews/{} для поиска отзыва", id);
        return reviewService.findReviewById(id);
    }

    @GetMapping
    public List<ReviewDto> findAll(@RequestParam @Nullable Integer filmId, @RequestParam(defaultValue = "10") int count) {
        log.debug("Получен запрос GET /reviews для получения всех отзывов");
        return reviewService.findAll(filmId, count);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewDto createReview(@Valid @RequestBody ReviewRequest reviewRequest) {
        log.debug("Получен запрос POST /reviews для создания отзыва: {}", reviewRequest);
        return reviewService.createReview(reviewRequest);
    }

    @PutMapping
    public ReviewDto updateReview(@Valid @RequestBody ReviewRequestUpdate reviewRequestUpdate) {
        log.debug("Получен запрос PUT /reviews для обновления отзыва с id {}: {}", reviewRequestUpdate.getReviewId(), reviewRequestUpdate);
        return reviewService.updateReview(reviewRequestUpdate.getReviewId(), reviewRequestUpdate);
    }

    @DeleteMapping("/{reviewId}")
    public void removeReview(@PathVariable Integer reviewId) {
        log.debug("Получен запрос DELETE /reviews/{} для удаления отзыва", reviewId);
        reviewService.removeReview(reviewId);
    }

    @PutMapping("/{reviewId}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void addLike(@PathVariable Integer reviewId, @PathVariable Integer userId) {
        log.debug("Получен запрос PUT /reviews/{}/like/{} для добавления лайка отзыву", reviewId, userId);
        reviewService.addLike(reviewId, userId);
    }

    @DeleteMapping("/{reviewId}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteLike(@PathVariable Integer reviewId, @PathVariable Integer userId) {
        log.debug("Получен запрос DELETE /reviews/{}/like/{} для удаления лайка отзыва", reviewId, userId);
        reviewService.removeLike(reviewId, userId);
    }

    @PutMapping("/{reviewId}/dislike/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void addDislike(@PathVariable Integer reviewId, @PathVariable Integer userId) {
        log.debug("Получен запрос PUT /reviews/{}/dislike/{} для добавления дизлайка отзыву", reviewId, userId);
        reviewService.addDislike(reviewId, userId);
    }

    @DeleteMapping("/{reviewId}/dislike/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteDislike(@PathVariable Integer reviewId, @PathVariable Integer userId) {
        log.debug("Получен запрос DELETE /reviews/{}/dislike/{} для удаления дизлайка отзыва", reviewId, userId);
        reviewService.removeDislike(reviewId, userId);
    }

}