
package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;


public interface ReviewStorage {
    Optional<Review> findReviewById(int reviewId);

    List<Review> findAllReviews();

    List<Review> findAllReviews(Integer count);

    List<Review> findAllReviews(Integer filmId, Integer count);

    Review createReview(Review review);

    Review updateReview(Review reviewForUpdate);

    void removeReview(int reviewId);

    void addLike(int reviewId, int userId);

    void removeLike(int reviewId, int userId);

    void addDislike(int reviewId, int userId);

    void removeDislike(int reviewId, int userId);

    Optional<Boolean> getUserReviewRating(int reviewId, int userId);
}
