package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.EventRowMapper;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Event;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class EventRepository {
    private static final String GET_EVENTS_BY_USERID = " SELECT * FROM EVENTS WHERE USER_ID = ? ";
    private static final String ADD_EVENT = "INSERT INTO EVENTS (USER_ID, ENTITY_ID, TIMESTAMP, EVENT_TYPE, OPERATION)" +
            "VALUES(?, ?, ?, ?, ?)";

    private final JdbcTemplate jdbc;
    private final EventRowMapper mapper;

    public List<Event> getEventByUserId(Integer userId) {
        try {
            return jdbc.query(GET_EVENTS_BY_USERID, mapper, userId);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public Event addEvent(Event newEvent) {
       Integer id = insert(ADD_EVENT,
                newEvent.getUserId(),
                newEvent.getEntityId(),
                newEvent.getTimestamp(),
                newEvent.getEventType().name(),
                newEvent.getOperation().name());
        newEvent.setId(id);
        return newEvent;
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
}
