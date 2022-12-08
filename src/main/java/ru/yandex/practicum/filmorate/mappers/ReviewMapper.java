package ru.yandex.practicum.filmorate.mappers;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ReviewMapper implements RowMapper<Review> {

    @Override
    public Review mapRow(ResultSet rs, int rowNum) throws SQLException {
        Review review = new Review(rs.getInt("user_id")
                , rs.getInt("film_id")
                , rs.getString("content"));

        review.setReviewId(rs.getInt("review_id"));
        review.setIsPositive(rs.getBoolean("is_positive"));
        review.setUseful(rs.getInt("useful"));

        return review;
    }
}
