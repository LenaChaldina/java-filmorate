package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Objects;

@Data
public class Review {
    private Integer reviewId;
    @NotNull
    private final Integer userId;
    @NotNull
    private final Integer filmId;
    private Boolean isPositive;
    @NotNull
    private final String content;
    private Integer useful;

    @JsonGetter("isPositive")
    public Boolean isPositive() {
        return isPositive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Review review = (Review) o;
        return Objects.equals(reviewId, review.reviewId) && Objects.equals(userId, review.userId) && Objects.equals(filmId, review.filmId) && Objects.equals(isPositive, review.isPositive) && Objects.equals(content, review.content) && Objects.equals(useful, review.useful);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reviewId, userId, filmId, isPositive, content, useful);
    }
}
