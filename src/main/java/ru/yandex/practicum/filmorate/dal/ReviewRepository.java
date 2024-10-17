package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.ReviewRowMapper;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.OperationType;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.TypeOfEvent;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ReviewRepository {
    private final EventRepository eventRepository;
    private static final String FIND_ALL_REVIEW = "SELECT * FROM REVIEWS";
    private static final String FIND_REVIEW_BY_ID = "SELECT * FROM REVIEWS WHERE REVIEW_ID = ?";
    private static final String ADD_REVIEW = "INSERT INTO REVIEWS (CONTENT, IS_POSITIVE, USER_ID, FILM_ID, USEFUL)" +
            "VALUES(?, ?, ?, ?, COALESCE(?, 0))";
    private static final String UPDATE_REVIEW = """
            UPDATE REVIEWS
            SET CONTENT = ?, IS_POSITIVE = ?
            WHERE REVIEW_ID = ?
            """;
    private static final String DELETE_REVIEW = "DELETE FROM REVIEWS WHERE REVIEW_ID = ?";
    private static final String UPDATE_USEFUL = "UPDATE REVIEWS SET USEFUL=?" +
            "WHERE REVIEW_ID =?";
    private final JdbcTemplate jdbc;
    private final ReviewRowMapper mapper;

    public List<Review> getAllReview(Integer count) {
        return jdbc.query(FIND_ALL_REVIEW + " LIMIT ?", mapper, count);
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
        eventRepository.addEvent(Event.builder()
                .userId(newReview.getUserId())
                .eventType(TypeOfEvent.REVIEW)
                .operation(OperationType.ADD)
                .timestamp(Instant.now().toEpochMilli())
                .entityId(id)
                .build());
        log.warn("{} оставил ревью на {}", newReview.getUserId(), newReview.getFilmId());
        return newReview;
    }

    public Review updateReview(Review newReview) {
        Review review;
        try {
            review = jdbc.queryForObject(FIND_REVIEW_BY_ID, mapper, newReview.getReviewId());
        } catch (DataAccessException e) {
            throw new NotFoundException("Ревью с таким ID не найден");
        }
        jdbc.update(UPDATE_REVIEW, newReview.getContent(), newReview.getIsPositive(),
                newReview.getReviewId());
        eventRepository.addEvent(Event.builder()
                .userId(review.getUserId())
                .eventType(TypeOfEvent.REVIEW)
                .operation(OperationType.UPDATE)
                .timestamp(Instant.now().toEpochMilli())
                .entityId(review.getReviewId())
                .build());
        log.warn("{} обновил ревью на {}", review.getUserId(), review.getFilmId());
        return jdbc.queryForObject(FIND_REVIEW_BY_ID, mapper, newReview.getReviewId());
    }

    public void deleteReview(Integer id) {
        Review reviewToDelete = getReviewById(id);
        try {
            getReviewById(id);
        } catch (DataAccessException e) {
            throw new NotFoundException("Ревью с ID = " + id + " не найдено");
        }
        jdbc.update(DELETE_REVIEW, id);
        eventRepository.addEvent(Event.builder()
                .userId(reviewToDelete.getUserId())
                .eventType(TypeOfEvent.REVIEW)
                .operation(OperationType.REMOVE)
                .timestamp(Instant.now().toEpochMilli())
                .entityId(id)
                .build());
        log.warn("{} удалил ревью {}", reviewToDelete.getUserId(), id);
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
        query = query + " ORDER BY USEFUL DESC ";
        query = query + " LIMIT " + count;

        return jdbc.query(query, mapper);
    }

    public void innerUpdate(Review review) {
        jdbc.update(UPDATE_USEFUL, review.getUseful(),
                review.getReviewId());
    }

}
