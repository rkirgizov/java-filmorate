package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.BaseStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component("ReviewStorageDbImpl")
public class ReviewStorageDbImpl extends BaseStorage<Review> implements ReviewStorage {
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM _review WHERE id = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM _review ORDER BY useful";
    private static final String FIND_ALL_LIMIT_QUERY = "SELECT * FROM _review ORDER BY useful DESC LIMIT ?";
    private static final String FIND_ALL_FILMID_LIMIT_QUERY = "SELECT * FROM _review WHERE film_id = ? ORDER BY useful DESC LIMIT ?";
    private static final String INSERT_QUERY = "INSERT INTO _review (content, is_positive, user_id, film_id) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE _review SET content = ?, is_positive = ?, user_id = ?, film_id = ? WHERE id = ?";
    private static final String DELETE_QUERY = "DELETE FROM _review WHERE id = ?";
    private static final String INSERT_REVIEW_RATING_QUERY = "INSERT INTO _review_rating (review_id, user_id, is_like) VALUES (?, ?, ?)";
    private static final String DELETE_REVIEW_RATING_QUERY = "DELETE FROM _review_rating WHERE review_id = ? AND user_id = ?";
    private static final String UPDATE_USEFUL_INCREMENT_QUERY = "UPDATE _review SET useful = useful + ? WHERE id = ?";
    private static final String GET_USER_REVIEW_RATING_QUERY = "SELECT is_like FROM _review_rating WHERE review_id = ? AND user_id = ?";

    public ReviewStorageDbImpl(JdbcTemplate jdbc, RowMapper<Review> mapper) {
        super(jdbc, mapper);
    }

    public Optional<Review> findReviewById(int reviewId) {
        log.debug("Вывод отзыва по id: {}", reviewId);
        return findOne(FIND_BY_ID_QUERY, reviewId);
    }

    @Override
    public List<Review> findAllReviews() {
        log.debug("Вывод списка всех отзывов");
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public List<Review> findAllReviews(Integer count) {
        log.debug("Вывод списка отзывов c ограничением по количеству: {}", count);
        return findMany(FIND_ALL_LIMIT_QUERY, count);
    }

    @Override
    public List<Review> findAllReviews(Integer filmId, Integer count) {
        log.debug("Вывод списка отзывов по id фильма: {} с ограничением по количеству: {}", filmId, count);
        return findMany(FIND_ALL_FILMID_LIMIT_QUERY, filmId, count);
    }

    public Review createReview(Review review) {
        int id = insert(
                INSERT_QUERY,
                review.getContent(),
                review.getIsPositive(),
                review.getUserId(),
                review.getFilmId()
        );
        review.setId(id);
        log.debug("Отзыв с id = {} - добавлен", review.getId());
        return review;
    }

    @Override
    public Review updateReview(Review review) {
        update(
                UPDATE_QUERY,
                review.getContent(),
                review.getIsPositive(),
                review.getUserId(),
                review.getFilmId(),
                review.getId());
        log.debug("Отзыв с id = {} - обновлен", review.getId());
        return review;
    }

    @Override
    public void removeReview(int reviewId) {
        log.debug("Отзыв с id = {} - удален", reviewId);
        delete(DELETE_QUERY, reviewId);
    }

    @Override
    public void addLike(int reviewId, int userId) {
        insert(
                INSERT_REVIEW_RATING_QUERY,
                reviewId,
                userId,
                true
        );
        jdbc.update(
                UPDATE_USEFUL_INCREMENT_QUERY,
                1,
                reviewId
        );
        log.debug("Отзыв с id = {} получил лайк от пользователя с id = {}", reviewId, userId);
    }

    @Override
    public void removeLike(int reviewId, int userId) {
        delete(DELETE_REVIEW_RATING_QUERY, reviewId, userId);
        jdbc.update(
                UPDATE_USEFUL_INCREMENT_QUERY,
                -1,
                reviewId
        );
        log.debug("Отзыв с id = {} потерял лайк от пользователя с id = {}", reviewId, userId);
    }

    @Override
    public void addDislike(int reviewId, int userId) {
        insert(
                INSERT_REVIEW_RATING_QUERY,
                reviewId,
                userId,
                false
        );
        jdbc.update(
                UPDATE_USEFUL_INCREMENT_QUERY,
                -1,
                reviewId
        );
        log.debug("Отзыв с id = {} получил дизлайк от пользователя с id = {}", reviewId, userId);
    }

    @Override
    public void removeDislike(int reviewId, int userId) {
        delete(DELETE_REVIEW_RATING_QUERY, reviewId, userId);
        jdbc.update(
                UPDATE_USEFUL_INCREMENT_QUERY,
                1,
                reviewId
        );
        log.debug("Отзыв с id = {} потерял дизлайк от пользователя с id = {}", reviewId, userId);
    }

    @Override
    public Optional<Boolean> getUserReviewRating(int reviewId, int userId) {
        try {
            return Optional.ofNullable(jdbc.queryForObject(GET_USER_REVIEW_RATING_QUERY, Boolean.class, reviewId, userId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

}












