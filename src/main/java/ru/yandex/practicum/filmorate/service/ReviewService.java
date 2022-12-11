package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FeedStorage;
import ru.yandex.practicum.filmorate.dao.ReviewStorage;
import ru.yandex.practicum.filmorate.dao.impl.FilmDbStorage;
import ru.yandex.practicum.filmorate.dao.impl.ReviewDbStorage;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.OperationType;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.RequestError;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;

@Service
@Slf4j
public class ReviewService implements ReviewStorage {

    private int id = 1;

    private final ReviewDbStorage reviewDbStorage;
    private final FilmDbStorage filmDbStorage;
    private final UserService userService;
    private final FeedStorage feedStorage;

    public ReviewService(@Qualifier("reviewDbStorage") ReviewDbStorage reviewDbStorage
            , UserService userService, @Qualifier("FilmDbStorage") FilmDbStorage filmDbStorage, FeedStorage feedStorage) {
        this.reviewDbStorage = reviewDbStorage;
        this.userService = userService;
        this.filmDbStorage = filmDbStorage;
        this.feedStorage = feedStorage;
    }

    @Override
    public Review createReview(Review review) {
        if (review.isPositive() == null) {
            log.warn("Отзыв не создан. Не указан тип отзыва");
            throw new RequestError(HttpStatus.BAD_REQUEST, "Не указан тип отзыва");
        }
        if (!userService.checkOnContainsUser(review.getUserId())) {
            log.warn("Отзыв не создан, пользователь с id {} не найден", review.getUserId());
            throw new EntityNotFoundException("Пользователь с таким id не найден");
        }
        if (filmDbStorage.checkFilmIdExists(review.getFilmId())) {
            log.warn("Отзыв не создан, фильм с id {} не найден", review.getFilmId());
            throw new EntityNotFoundException("Фильм с таким id не найден");
        }
        if (review.getReviewId() == null) {
            review.setReviewId(id);
            log.info("Отзыву пользователя {} для фильма {} автоматически присвоен id = {}"
                    , review.getUserId(), review.getFilmId(), id);
            id++;
        }
        log.info("Отзыв с id = {} успешно создан", review.getReviewId());
        Review newReview = reviewDbStorage.createReview(review);
        feedStorage.addFeedEvent(Feed.builder().userId(newReview.getUserId()).entityId(newReview.getReviewId()).eventType(EventType.REVIEW).operation(OperationType.ADD).build());
        return newReview;
    }

    @Override
    public Review updateReview(Review review) {
        if (checkContainsReview(review.getReviewId())) {
            log.warn("Отзыв не обновлен, отзыва с id {} не найдено", review.getReviewId());
            throw new EntityNotFoundException("Отзыв с таким id не найден");
        }
        log.info("Отзыв с id = {} успешно обновлен", review.getReviewId());
        Review updatedReview = reviewDbStorage.updateReview(review);
        feedStorage.addFeedEvent(Feed.builder().userId(updatedReview.getUserId()).entityId(updatedReview.getFilmId()).eventType(EventType.REVIEW).operation(OperationType.UPDATE).build());
        return updatedReview;
    }

    @Override
    public void deleteReview(int reviewId) {
        if (checkContainsReview(reviewId)) {
            log.warn("Отзыв не удален, отзыва с id {} не найдено", reviewId);
            throw new EntityNotFoundException("Отзыв с таким id не найден");
        }
        log.info("Отзыв с id = {} успешно удален", reviewId);
        feedStorage.addFeedEvent(Feed.builder().entityId(reviewId).eventType(EventType.REVIEW).operation(OperationType.REMOVE).build());

        reviewDbStorage.deleteReview(reviewId);
    }

    @Override
    public Review getReviewById(int reviewId) {
        if (checkContainsReview(reviewId)) {
            log.warn("Не возможно отобразить отзыв, отзыва с id {} не найдено", reviewId);
            throw new EntityNotFoundException("Отзыв с таким id не найден");
        }
        log.info("Отзыв с id = {} отображен", reviewId);
        return reviewDbStorage.getReviewById(reviewId);
    }

    @Override
    public Collection<Review> getAllReviewByFilmId(int filmId, int count) {
        if (filmDbStorage.checkFilmIdExists(filmId)) {
            log.warn("Отзыв не создан, фильм с id {} не найден", filmId);
            throw new EntityNotFoundException("Фильм с таким id не найден");
        }
        return reviewDbStorage.getAllReviewByFilmId(filmId, count);
    }

    @Override
    public Collection<Review> getAllReview() {
        return reviewDbStorage.getAllReview();
    }

    @Override
    public void addLikeForReview(int reviewId, int userId) {
        if (checkContainsReview(reviewId)) {
            log.warn("Невозможно добавить лайк отзыву, отзыва с id {} не найдено", reviewId);
            throw new EntityNotFoundException("Отзыв с таким id не найден");
        }
        if (!userService.checkOnContainsUser(userId)) {
            log.warn("Лайк не добавлен, пользователь с id {} не найден", userId);
            throw new EntityNotFoundException("Пользователь с таким id не найден");
        }
        if (reviewDbStorage.checkContainsLikeInReview(reviewId, userId)) {
            log.warn("Лайк не добавлен, пользователь с id {} уже добавил лайк посту с id {}", userId, reviewId);
            throw new EntityNotFoundException("Пользователь уже добавил лайк");
        }

        reviewDbStorage.addLikeForReview(reviewId, userId);
    }

    @Override
    public void addDislikeForReview(int reviewId, int userId) {
        if (checkContainsReview(reviewId)) {
            log.warn("Невозможно добавить дизлайк отзыву, отзыва с id {} не найдено", reviewId);
            throw new EntityNotFoundException("Отзыв с таким id не найден");
        }
        if (!userService.checkOnContainsUser(userId)) {
            log.warn("Невозможно добавить дизлайк, пользователь с id {} не найден", userId);
            throw new EntityNotFoundException("Пользователь с таким id не найден");
        }
        if (reviewDbStorage.checkContainsLikeInReview(reviewId, userId)) {
            log.warn("Дизлайк не добавлен, пользователь с id {} уже добавил дизлайк посту с id {}", userId, reviewId);
            throw new EntityNotFoundException("Пользователь уже добавил дизлайк");
        }

        reviewDbStorage.addDislikeForReview(reviewId, userId);
    }

    @Override
    public void deleteLikeForReview(int reviewId, int userId) {
        if (checkContainsReview(reviewId)) {
            log.warn("Невозможно удалить лайк отзыву, отзыва с id {} не найдено", reviewId);
            throw new EntityNotFoundException("Отзыв с таким id не найден");
        }
        if (!userService.checkOnContainsUser(userId)) {
            log.warn("Лайк не удален, пользователь с id {} не найден", userId);
            throw new EntityNotFoundException("Пользователь с таким id не найден");
        }
        if (reviewDbStorage.checkContainsLikeInReview(reviewId, userId)) {
            log.warn("Лайк не удален, пользователь с id {} не ставил лайк посту с id {}", userId, reviewId);
            throw new EntityNotFoundException("Пользователь уже добавил лайк");
        }

        reviewDbStorage.deleteLikeForReview(reviewId, userId);
    }

    @Override
    public void deleteDislikeForReview(int reviewId, int userId) {
        if (checkContainsReview(reviewId)) {
            log.warn("Невозможно удалить дизлайк отзыву, отзыва с id {} не найдено", reviewId);
            throw new EntityNotFoundException("Отзыв с таким id не найден");
        }
        if (!userService.checkOnContainsUser(userId)) {
            log.warn("Невозможно удалить дизлайк, пользователь с id {} не найден", userId);
            throw new EntityNotFoundException("Пользователь с таким id не найден");
        }
        if (reviewDbStorage.checkContainsLikeInReview(reviewId, userId)) {
            log.warn("Дислайк не удален, пользователь с id {} не ставил дислайк посту с id {}", userId, reviewId);
            throw new EntityNotFoundException("Пользователь уже добавил дислайк");
        }
    }

    private boolean checkContainsReview(int reviewId) {
        for (Review review : reviewDbStorage.getAllReview()) {
            if (review.getReviewId() == reviewId) return false;
        }
        return true;
    }
}
