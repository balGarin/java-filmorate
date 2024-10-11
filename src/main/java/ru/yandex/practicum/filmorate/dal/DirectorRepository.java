package ru.yandex.practicum.filmorate.dal;

import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.exception.IncorrectDataException;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
@AllArgsConstructor
public class DirectorRepository {

    private final JdbcTemplate jdbc;
    private final DirectorRowMapper directorRowMapper;

    private static final String ADD_NEW_DIRECTOR = "INSERT INTO DIRECTORS (DIRECTOR_NAME) " +
            "VALUES(?)";

    private static final String GET_ALL_DIRECTORS = "SELECT * FROM DIRECTORS ";

    private static final String GET_DIRECTOR_BY_ID = "SELECT * FROM DIRECTORS WHERE DIRECTOR_ID = ?";

    private static final String UPDATE_DIRECTOR = "UPDATE DIRECTORS SET " +
            "DIRECTOR_NAME = ? " +
            "WHERE DIRECTOR_ID = ?";

    private static final String DELETE_DIRECTOR_BY_ID = "DELETE FROM DIRECTORS WHERE DIRECTOR_ID = ?";

    public Director addDirector(Director newDirector) {
        int id = insert(ADD_NEW_DIRECTOR, newDirector.getName());
        newDirector.setId(id);
        return newDirector;
    }

    public List<Director> getAllDirectors() {
        return jdbc.query(GET_ALL_DIRECTORS, directorRowMapper);
    }

    public Director updateDirector(Director newDirector) {
        Director director;
        if (newDirector.getId() == null) {
            throw new IncorrectDataException("Поле ID для этой операции обязателен");
        }
        try {
            director = jdbc.queryForObject(GET_DIRECTOR_BY_ID, directorRowMapper, newDirector.getId());
            director.setName(newDirector.getName());
            jdbc.update(UPDATE_DIRECTOR, director.getName(), director.getId());
            return jdbc.queryForObject(GET_DIRECTOR_BY_ID, directorRowMapper, director.getId());
        } catch (DataAccessException e) {
            throw new NotFoundException("Режиссер с id " + newDirector.getId() + " не найден");
        }
    }

    public void deleteDirectorById(Integer id) {
        int rowDeleted = jdbc.update(DELETE_DIRECTOR_BY_ID, id);
        if (rowDeleted == 0) {
            throw new NotFoundException("Режиссер с ID " + id + " не найден");
        }
    }

    public Director getDirectorById(Integer id) {
        try {
            return jdbc.queryForObject(GET_DIRECTOR_BY_ID, directorRowMapper, id);
        } catch (DataAccessException e) {
            throw new NotFoundException("Режиссер с id " + id + " не найден");
        }
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
