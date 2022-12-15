package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping()
    public Review createReview(@Valid @RequestBody Review review) {
        return reviewService.createReview(review);
    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody Review review) {
        return reviewService.updateReview(review);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable Integer id) {
        reviewService.deleteReview(id);
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable Integer id) {
        return reviewService.getReviewById(id);
    }

    @GetMapping()
    public Collection<Review> getAllReviewByFilmId(@RequestParam(value = "filmId", required = false) Integer filmId
            , @RequestParam(value = "count", defaultValue = "10", required = false) Integer count) {
        if (filmId == null) {
            return reviewService.getAllReview();
        }
        return reviewService.getAllReviewByFilmId(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLikeForReview(@PathVariable Integer id, @PathVariable Integer userId) {
        reviewService.addLikeForReview(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislikeForReview(@PathVariable Integer id, @PathVariable Integer userId) {
        reviewService.addDislikeForReview(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLikeForReview(@PathVariable Integer id, @PathVariable Integer userId) {
        reviewService.deleteLikeForReview(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislikeForReview(@PathVariable Integer id, @PathVariable Integer userId) {
        reviewService.deleteDislikeForReview(id, userId);
    }
}
