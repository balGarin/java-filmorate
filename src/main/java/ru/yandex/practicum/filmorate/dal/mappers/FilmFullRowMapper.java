package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

@Component
public class FilmFullRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(resultSet.getInt(1));
        film.setName(resultSet.getString(2));
        film.setDescription(resultSet.getString(3));
        film.setReleaseDate(resultSet.getDate(4).toLocalDate());
        film.setDuration(resultSet.getInt(5));
        MPA mpa = new MPA();
        mpa.setId(resultSet.getInt(6));
        mpa.setName(resultSet.getString(7));
        film.setMpa(mpa);
        Set<Genre> genres = new HashSet<>();
        Genre genre = new Genre();
        genre.setId(resultSet.getInt(8));
        genre.setName(resultSet.getString(9));
        genres.add(genre);
        film.setGenres(genres);
        Set<Director> directors = new HashSet<>();
        if (resultSet.getInt("DIRECTOR_ID") != 0 && !resultSet.getString("DIRECTOR_NAME").equals(null)) {
            Director director = new Director();
            director.setId(resultSet.getInt("DIRECTOR_ID"));
            director.setName(resultSet.getString("DIRECTOR_NAME"));
            directors.add(director);
        }
        film.setDirectors(directors);
        return film;
    }
}
