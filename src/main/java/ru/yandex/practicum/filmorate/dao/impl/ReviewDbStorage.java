package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.ReviewStorage;
import ru.yandex.practicum.filmorate.mappers.ReviewMapper;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Repository("reviewDbStorage")
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;

    public ReviewDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Review createReview(Review review) {
        jdbcTemplate.update("INSERT INTO REVIEWS (USER_ID, FILM_ID, IS_POSITIVE, CONTENT, USEFUL) " +
                        "VALUES (?, ?, ?, ?, ?)", review.getUserId(), review.getFilmId()
                , review.isPositive(), review.getContent(), 0);

        return getReviewById(review.getReviewId());
    }

    @Override
    public Review updateReview(Review review) {
        jdbcTemplate.update("UPDATE REVIEWS SET CONTENT = ?, IS_POSITIVE = ? WHERE REVIEW_ID = ?"
                , review.getContent(), review.isPositive(), review.getReviewId());

        return getReviewById(review.getReviewId());
    }

    @Override
    public void deleteReview(int reviewId) {
        jdbcTemplate.update("DELETE FROM REVIEW_LIKES WHERE REVIEW_ID = ?", reviewId);
        jdbcTemplate.update("DELETE FROM REVIEWS WHERE REVIEW_ID = ?", reviewId);
    }

    @Override
    public Review getReviewById(int reviewId) {
        List<Review> reviewList = jdbcTemplate.query("SELECT * FROM REVIEWS WHERE REVIEW_ID = ?"
                , new ReviewMapper(), reviewId);
        if(reviewList.size() == 0) {
            return null;
        }
        return reviewList.get(0);
    }

    @Override
    public Collection<Review> getAllReviewByFilmId(int filmId, int count) {
        Collection<Review> reviews = jdbcTemplate.query("SELECT * FROM REVIEWS WHERE FILM_ID = ? " +
                        "ORDER BY USEFUL DESC"
                , new ReviewMapper(), filmId);

        return reviews.stream().limit(count).collect(Collectors.toList());
    }

    @Override
    public Collection<Review> getAllReview() {
        return jdbcTemplate.query("SELECT * FROM REVIEWS ORDER BY USEFUL DESC"
                , new ReviewMapper());
    }

    @Override
    public void addLikeForReview(int reviewId, int userId) {
        Review review = getReviewById(reviewId);
        int newUseful = review.getUseful() + 1;

        jdbcTemplate.update("INSERT INTO REVIEW_LIKES (REVIEW_ID, USER_ID, GRADE) VALUES ( ?, ?, ? )"
                , reviewId, userId, "like");
        jdbcTemplate.update("UPDATE REVIEWS SET USEFUL = ? WHERE REVIEW_ID = ?", newUseful, reviewId);
    }

    @Override
    public void addDislikeForReview(int reviewId, int userId) {
        Review review = getReviewById(reviewId);
        int newUseful = review.getUseful() - 1;

        jdbcTemplate.update("INSERT INTO REVIEW_LIKES (REVIEW_ID, USER_ID, GRADE) VALUES ( ?, ?, ? )"
                , reviewId, userId, "dislike");
        jdbcTemplate.update("UPDATE REVIEWS SET USEFUL = ? WHERE REVIEW_ID = ?", newUseful, reviewId);
    }

    @Override
    public void deleteLikeForReview(int reviewId, int userId) {
        Review review = getReviewById(reviewId);
        int newUseful = review.getUseful() - 1;

        jdbcTemplate.update("DELETE FROM REVIEW_LIKES WHERE REVIEW_ID = ? AND USER_ID = ? AND GRADE = ?"
                , reviewId, userId, "like");
        jdbcTemplate.update("UPDATE REVIEWS SET USEFUL = ? WHERE REVIEW_ID = ?", newUseful, reviewId);
    }

    @Override
    public void deleteDislikeForReview(int reviewId, int userId) {
        Review review = getReviewById(reviewId);
        int newUseful = review.getUseful() + 1;

        jdbcTemplate.update("DELETE FROM REVIEW_LIKES WHERE REVIEW_ID = ? AND USER_ID = ? AND GRADE = ?"
                , reviewId, userId, "dislike");
        jdbcTemplate.update("UPDATE REVIEWS SET USEFUL = ? WHERE REVIEW_ID = ?", newUseful, reviewId);
    }

    public boolean checkContainsLikeInReview(Integer reviewId, Integer userId) {
        SqlRowSet likeRows = jdbcTemplate.queryForRowSet("SELECT * FROM REVIEW_LIKES " +
                "WHERE REVIEW_ID = ? AND GRADE = ? AND USER_ID = ?", reviewId, "like", userId);
        return likeRows.next();
    }
}
