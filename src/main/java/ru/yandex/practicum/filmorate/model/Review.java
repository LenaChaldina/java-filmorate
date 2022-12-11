package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotNull;
import java.util.Objects;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Review {
    Integer reviewId;
    @NotNull
    final Integer userId;
    @NotNull
    final Integer filmId;
    Boolean isPositive;
    @NotNull
    final String content;
    Integer useful;

    @JsonGetter("isPositive")
    public Boolean isPositive() {
        return isPositive;
    }
}
