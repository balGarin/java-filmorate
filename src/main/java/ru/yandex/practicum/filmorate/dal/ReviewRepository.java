package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.ReviewRowMapper;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReviewRepository {
    private static final String FIND_ALL_REVIEW = "SELECT * FROM REVIEWS";
    private static final String FIND_REVIEW_BY_ID = "SELECT * FROM REVIEWS WHERE REVIEW_ID = ?";
    private static final String ADD_REVIEW = "INSERT INTO REVIEWS (CONTENT, IS_POSITIVE, USER_ID, FILM_ID, USEFUL)" +
            "VALUES(?, ?, ?, ?, COALESCE(?, 0))";
    private static final String UPDATE_REVIEW = """
        UPDATE REVIEWS
        SET CONTENT = ?, IS_POSITIVE = ?, USER_ID = ?, FILM_ID = ?, USEFUL = ?
        WHERE REVIEW_ID = ?
        """;
    private static final String DELETE_REVIEW = "DELETE FROM REVIEWS WHERE REVIEW_ID = ?";

    private final JdbcTemplate jdbc;
    private final ReviewRowMapper mapper;


    public List<Review> getAllReview() {
        return jdbc.query(FIND_ALL_REVIEW, mapper);
    }

    public Review getReviewById(Integer id) {
        try {
            return jdbc.queryForObject(FIND_REVIEW_BY_ID, mapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Id ревью " + id + " не найден!");
        }
    }

    public Review addReview(Review newReview) {
        Integer id = insert(ADD_REVIEW, newReview.getContent(), newReview.getIsPositive(), newReview.getUserId(),
                newReview.getFilmId(), newReview.getUseful());
        newReview.setReviewId(id);
        return newReview;
    }

    public Review updateReview(Review newReview) {
        jdbc.update(UPDATE_REVIEW, newReview.getContent(), newReview.getIsPositive(), newReview.getUserId(),
                newReview.getFilmId(),
                newReview.getUseful(),
                newReview.getReviewId());
        return newReview;
    }

    public void deleteReview(Integer id) {
        try {
            getReviewById(id);
        } catch (DataAccessException e) {
            throw new NotFoundException("Ревью с ID = " + id + " не найдено");
        }
        jdbc.update(DELETE_REVIEW, id);
    }


    private Integer insert(String query, Object... params) {
        try {


            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
            jdbc.update(connection -> {
                PreparedStatement ps = connection
                        .prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                for (int idx = 0; idx < params.length; idx++) {
                    ps.setObject(idx + 1, params[idx]);
                }
                return ps;
            }, keyHolder);

            Integer id = keyHolder.getKeyAs(Integer.class);

            if (id != null) {
                return id;
            } else {
                throw new InternalServerException("Не удалось сохранить данные");
            }
        } catch (DataAccessException e) {
            throw new ValidationException(e.getMessage());
        }
    }

    public List<Review> getReviewsByFilmId(Integer filmId, Integer count) {
        String query = FIND_ALL_REVIEW;
        if (filmId != null) {
            query = query + " WHERE FILM_ID = " + filmId;
        }
        query = query + " LIMIT " + count;

        return jdbc.query(query, mapper);
    }
}
