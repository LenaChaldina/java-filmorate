package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.ReviewStorage;
import ru.yandex.practicum.filmorate.mappers.ReviewMapper;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.*;
import java.util.stream.Collectors;

@Component("reviewDbStorage")
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;

    Comparator<Review> comparator = Comparator.comparing(Review::getUseful).reversed()
            .thenComparing(Review::getReviewId);

    public ReviewDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Review createReview(Review review) {
        jdbcTemplate.update("INSERT INTO REVIEWS (USER_ID, FILM_ID, IS_POSITIVE, CONTENT, USEFUL) " +
                        "VALUES (?, ?, ?, ?, ?)", review.getUserId(), review.getFilmId()
                , review.isPositive(), review.getContent(), review.getUseful());

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
        return jdbcTemplate.queryForObject("SELECT * FROM REVIEWS WHERE REVIEW_ID = ?"
                , new ReviewMapper(), reviewId);
    }

    @Override
    public Collection<Review> getAllReviewByFilmId(int filmId, int count) {
        Collection<Review> reviews = jdbcTemplate.query("SELECT * FROM REVIEWS WHERE FILM_ID = ?"
                , new ReviewMapper(), filmId);

        Set<Review> reviewForFilm = new TreeSet<>(comparator);
        reviewForFilm.addAll(reviews);

        return reviewForFilm.stream().limit(count).collect(Collectors.toList());
    }

    @Override
    public Collection<Review> getAllReview() {
        Collection<Review> reviews = jdbcTemplate.query("SELECT * FROM REVIEWS", new ReviewMapper());
        Set<Review> popularReview = new TreeSet<>(comparator);
        popularReview.addAll(reviews);

        return popularReview;
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

    public boolean checkContainsDislikeInReview(Integer reviewId, Integer userId) {
        SqlRowSet likeRows = jdbcTemplate.queryForRowSet("SELECT * FROM REVIEW_LIKES " +
                "WHERE REVIEW_ID = ? AND GRADE = ? AND USER_ID = ?", reviewId, "dislike", userId);
        return likeRows.next();
    }
}
