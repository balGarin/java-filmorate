package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReviewLikesRepository {
    private static final String DELETE_QUERY_COMMON = "DELETE FROM REVIEWS_LIKES WHERE REVIEW_ID = ? AND USER_ID = ?";
    private static final String DELETE_QUERY_LD = "DELETE FROM REVIEWS_LIKES WHERE REVIEW_ID = ? AND USER_ID = ?" +
            " AND IS_LIKE = ?";

    private static final String INSERT_QUERY = "INSERT INTO REVIEWS_LIKES(REVIEW_ID, USER_ID, IS_LIKE) " +
            "VALUES (?, ?, ?)";
    private static final String SELECT_USEFUL_QUERY = "select COALESCE(SUM(CASE WHEN IS_LIKE THEN 1 ELSE -1 END), 0) " +
            "USEFUL from reviews_likes where REVIEW_ID=?";

    private final JdbcTemplate jdbc;

    public void setLike(Integer reviewId, Integer userId) {
        commonDelete(reviewId, userId);
        commonInsert(reviewId, userId, true);
    }


    public void setDislike(Integer reviewId, Integer userId) {
        commonDelete(reviewId, userId);
        commonInsert(reviewId, userId, false);
    }

    public void deleteLike(Integer reviewId, Integer userId) {
        jdbc.update(DELETE_QUERY_LD, reviewId, userId, true);
    }

    public void deleteDislike(Integer reviewId, Integer userId) {
        jdbc.update(DELETE_QUERY_LD, reviewId, userId, false);
    }

    public Integer getUsableByReviewId(Integer reviewId) {
        return jdbc.queryForObject(SELECT_USEFUL_QUERY, Integer.class, reviewId);
    }

    private void commonDelete(Integer reviewId, Integer userId) {
        jdbc.update(DELETE_QUERY_COMMON, reviewId, userId);
    }

    private void commonInsert(Integer reviewId, Integer userId, Boolean isLike) {
        jdbc.update(INSERT_QUERY, reviewId, userId, isLike);
    }
}
