package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

@Component
public class FilmRowMapperCommon implements RowMapper<Film> {
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
        mpa.setName(resultSet.getString("RATING_NAME"));

        film.setMpa(mpa);

        String genres = resultSet.getString("GENRES");
        HashSet<Genre> genreHashSet = new HashSet<>();
        if (genres != null) {
            String[] splitGenres = genres.split(",");
            for (String strGenre : splitGenres) {
                String[] genreRow = strGenre.split("/");
                Genre g = new Genre();
                g.setId(Integer.parseInt(genreRow[0]));
                g.setName(genreRow[1]);
                genreHashSet.add(g);
            }
            film.setGenres(genreHashSet);
        }

        String likes = resultSet.getString("LIKES");
        if (likes != null) {
            film.setLikes(Arrays.stream(likes.split(",")).map(s -> Integer.parseInt(s)).collect(Collectors.toSet()));
        }
        return film;
    }
}
