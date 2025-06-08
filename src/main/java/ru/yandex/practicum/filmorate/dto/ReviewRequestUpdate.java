package ru.yandex.practicum.filmorate.dto;

import lombok.Getter;

@Getter
public class ReviewRequestUpdate extends ReviewRequest {
    private final Integer reviewId;

    public ReviewRequestUpdate(Integer reviewId, String content, Boolean isPositive, Integer userId, Integer filmId, int useful) {
        super(content, isPositive, userId, filmId, useful);
        this.reviewId = reviewId;
    }

}
