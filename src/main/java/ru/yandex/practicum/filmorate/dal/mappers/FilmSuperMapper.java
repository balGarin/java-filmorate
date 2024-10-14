package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
@Component
public class FilmSuperMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(resultSet.getInt("FILM_ID"));
        film.setName(resultSet.getString("FILM_NAME"));
        film.setDescription(resultSet.getString("DESCRIPTION"));
        film.setDuration(resultSet.getInt("DURATION"));
        film.setReleaseDate(resultSet.getDate("RELEASEDATE").toLocalDate());
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
        }else {
            film.setGenres(new HashSet<>());
        }
        String likes = resultSet.getString("LIKES");
        if (likes != null) {
            film.setLikes(Arrays.stream(likes.split(",")).map(s -> Integer.parseInt(s)).collect(Collectors.toSet()));
        }else {
            film.setLikes(new HashSet<>());
        }
        String directors = resultSet.getString("DIRECTORS");
        Set<Director>directorsSet = new HashSet<>();
        if(directors!=null){
            String[]splitDir = directors.split(",");
            for(String strDir : splitDir){
                String[] dirRow = strDir.split("/");
                Director director = new Director();
                director.setId(Integer.parseInt(dirRow[0]));
                director.setName((dirRow[1]));
                directorsSet.add(director);
            }
            film.setDirectors(directorsSet);
         }else {
            film.setDirectors(new HashSet<>());
        }
        return film;
    }
}
