package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MPA;

import java.sql.ResultSet;
import java.sql.SQLException;
@Component
public class FilmMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(resultSet.getInt("FILM_ID"));
        film.setName(resultSet.getString("FILM_NAME"));
        film.setDescription(resultSet.getString("DESCRIPTION"));
        film.setReleaseDate(resultSet.getDate("RELEASEDATE").toLocalDate());
        film.setDuration(resultSet.getInt("DURATION"));
        MPA mpa = new MPA();
        mpa.setId(resultSet.getInt("RATING_ID"));
        film.setMpa(mpa);
        return film;
    }
}
