package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;

public interface ReviewStorage {

    Review createReview(Review review);

    Review updateReview(Review review);

    void deleteReview(int reviewId);

    Review getReviewById(int reviewId);

    Collection<Review> getAllReviewByFilmId(int filmId, int count);

    Collection<Review> getAllReview();

    void addLikeForReview(int reviewId, int userId);

    void addDislikeForReview(int reviewId, int userId);

    void deleteLikeForReview(int reviewId, int userId);

    void deleteDislikeForReview(int reviewId, int userId);
}
