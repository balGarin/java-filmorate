package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ReviewRowMapper implements RowMapper<Review> {
    @Override
    public Review mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Review review = new Review();
        review.setReviewId(resultSet.getInt("REVIEW_ID"));
        review.setContent(resultSet.getString("CONTENT"));
        review.setIsPositive(resultSet.getBoolean("IS_POSITIVE"));
        review.setUserId(resultSet.getInt("USER_ID"));
        review.setFilmId(resultSet.getInt("FILM_ID"));
        review.setUseful(resultSet.getInt("USEFUL"));
        return review;
    }
}
