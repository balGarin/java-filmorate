package ru.yandex.practicum.filmorate.dal;

import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.*;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@AllArgsConstructor
@Repository("DBFilms")
public class FilmRepository implements FilmStorage {

    private final JdbcTemplate jdbc;
    private final FilmRowMapper filmRowMapper;
    private final MPARowMapper mpaRowMapper;
    private final GenreRowMapper genreRowMapper;
    private final UserRowMapper userRowMapper;
    private final LikeRowMapper likeRowMapper;
    private final FilmFullRowMapper filmFullRowMapper;
    private static final String ADD_FILM = "INSERT INTO FILMS (FILM_NAME,DESCRIPTION,RELEASEDATE,DURATION,RATING_ID)" +
            "VALUES(?,?,?,?,?)";


    private static final String FIND_ALL_FILMS = "SELECT f.FILM_ID ,f.FILM_NAME ,f.DESCRIPTION ,f.RELEASEDATE " +
            ",f.DURATION , f.RATING_ID " +
            "FROM  FILMS f ";

    private static final String FIND_FILM_BY_ID = "SELECT f.FILM_ID ,f.FILM_NAME ,f.DESCRIPTION ,f.RELEASEDATE " +
            ",f.DURATION , f.RATING_ID " +
            "FROM  FILMS f " +
            "WHERE f.FILM_ID = ?";
    private static final String FIND_MPA_BY_ID = "SELECT * FROM RATINGS WHERE RATING_ID = ?";

    private static final String FIND_GENRE_BY_FILM_ID = "SELECT g.GENRE_ID ,g.GENRE_NAME " +
            "FROM FILMS_GENRES fg JOIN GENRES g ON g.GENRE_ID =FG.GENRE_ID WHERE FG.FILM_ID = ?";


    private static final String UPDATE_FILM = "UPDATE FILMS SET FILM_ID =? , FILM_NAME = ? , DESCRIPTION = ?," +
            "DURATION = ?, RELEASEDATE = ?" +
            "WHERE FILM_ID = ?";

    private static final String ADD_LIKE = "INSERT INTO LIKES (FILM_ID,USER_ID)" +
            "VALUES (?,?)";

    private static final String FIND_USER_BY_ID = "SELECT * FROM USERS " +
            "WHERE USER_ID = ?";
    private static final String DELETE_LIKE = "DELETE FROM LIKES " +
            "WHERE FILM_ID=? AND USER_ID=?";

    private static final String GET_POPULAR = "SELECT f.FILM_ID ,f.FILM_NAME ,f.DESCRIPTION ,f.RELEASEDATE ," +
            "f.DURATION , f.RATING_ID   " +
            "FROM  FILMS f " +
            "JOIN LIKES l ON f.FILM_ID =l.FILM_ID " +
            "GROUP  BY f.FILM_ID  " +
            "ORDER BY COUNT(l.USER_ID) DESC " +
            "LIMIT ?";

    private static final String GET_LIST_OF_LIKES_BY_Id = "SELECT USER_ID FROM LIKES WHERE FILM_ID = ?";
    private static final String GET_ALL_FILMS_WITH_ALL_FIELDS = "SELECT f.FILM_ID,f.FILM_NAME,f.DESCRIPTION," +
            "f.RELEASEDATE,f.DURATION,f.RATING_ID,r.RATING_NAME,g.GENRE_ID,g.GENRE_NAME " +
            "FROM FILMS f " +
            "JOIN FILMS_GENRES fg ON f.FILM_ID =fg.FILM_ID " +
            "JOIN RATINGS r ON f.RATING_ID =r.RATING_ID " +
            "JOIN GENRES g ON fg.GENRE_ID = g.GENRE_ID ";

    private  static  final String DELETE_FILM_BY_ID ="";


    @Override
    public Film addFilm(Film film) {
        if (film.getMpa() == null) {
            film.setMpa(new MPA());
        }
        if (film.getGenres() == null) {
            film.setGenres(new HashSet<>());
        }
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }
        Integer id = insert(ADD_FILM, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(),
                film.getMpa().getId());
        film.setId(id);
        Set<Genre> genres = film.getGenres();
        addGenres(id, genres);
        return film;

    }

    @Override
    public Film updateFilm(Film newFilm) {
        try {
            Film film = jdbc.queryForObject(FIND_FILM_BY_ID, filmRowMapper, newFilm.getId());

        } catch (DataAccessException e) {
            throw new NotFoundException("Пользователь с таким ID не найден");
        }

        jdbc.update(UPDATE_FILM, newFilm.getId(), newFilm.getName(), newFilm.getDescription(), newFilm.getDuration(),
                newFilm.getReleaseDate(), newFilm.getId());
        return jdbc.queryForObject(FIND_FILM_BY_ID, filmRowMapper, newFilm.getId());
    }

    @Override
    public List<Film> getAllFilms() {
        List<Film> films = jdbc.query(FIND_ALL_FILMS, filmRowMapper);
        return fellFilms(films);
    }

    @Override
    public Film getFilmById(Integer id) {
        MPA mpa = new MPA();
        Film film = jdbc.queryForObject(FIND_FILM_BY_ID, filmRowMapper, id);
        if (film.getMpa() != null) {
            mpa = jdbc.queryForObject(FIND_MPA_BY_ID, mpaRowMapper, film.getMpa().getId());
        }
        film.setMpa(mpa);
        List<Genre> genres = jdbc.query(FIND_GENRE_BY_FILM_ID, genreRowMapper, film.getId());
        List<Integer> likes = jdbc.query(GET_LIST_OF_LIKES_BY_Id, likeRowMapper, film.getId());
        film.setLikes(new HashSet<>(likes));
        film.setGenres(new HashSet<>(genres));
        return film;


    }

    @Override
    public void addLike(Integer id, Integer userId) {
        try {
            User user = jdbc.queryForObject(FIND_USER_BY_ID, userRowMapper, userId);
        } catch (DataAccessException e) {
            throw new NotFoundException("Пользователя с " + userId + " ID не найден!");
        }
        try {
            Film film = jdbc.queryForObject(FIND_FILM_BY_ID, filmRowMapper, id);
        } catch (DataAccessException e) {
            throw new NotFoundException("Фильм с " + id + " ID не найден!");
        }

        insertForTwoKeys(ADD_LIKE, id, userId);
    }

    @Override
    public void deleteLike(Integer id, Integer userId) {
        try {
            User user = jdbc.queryForObject(FIND_USER_BY_ID, userRowMapper, userId);
        } catch (DataAccessException e) {
            throw new NotFoundException("Пользователя с " + userId + " ID не найден!");
        }
        try {
            Film film = jdbc.queryForObject(FIND_FILM_BY_ID, filmRowMapper, id);
        } catch (DataAccessException e) {
            throw new NotFoundException("Фильм с " + id + " ID не найден!");
        }
        int rowsDeleted = jdbc.update(DELETE_LIKE, id, userId);
        if (rowsDeleted == 0) {
            throw new NotFoundException(" Лайк не найден");
        }
    }

    @Override
    public List<Film> getMostPopularFilms(Integer count) {
        List<Film> films = jdbc.query(GET_POPULAR, filmRowMapper, count);
        return fellFilms(films);

    }


    private void insertForTwoKeys(String query, Object... params) {
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
        } catch (DataAccessException e) {
            throw new ValidationException(e.getMessage());
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
    private List<Film> fellFilms(List<Film> films) {
        List<Film> filmsWithAllFields = jdbc.query(GET_ALL_FILMS_WITH_ALL_FIELDS, filmFullRowMapper);
        for (Film rFilm : films) {
            List<Genre> genres = new ArrayList<>();
            MPA mpa = new MPA();
            for (Film fFilm : filmsWithAllFields) {
                if (rFilm.getId().equals(fFilm.getId())) {
                    genres.addAll(fFilm.getGenres());
                    mpa = fFilm.getMpa();
                }
            }
            rFilm.setGenres(new HashSet<>(genres));
            rFilm.setMpa(mpa);
        }
        return films;
    }

    public List<Integer> getLikes(Integer id) {
        return jdbc.query(GET_LIST_OF_LIKES_BY_Id, likeRowMapper, id);
    }

    private void addGenres(Integer id, Set<Genre> genres) {
        if (genres == null || genres.size() == 0) {
            return;
        }
        String query = "INSERT INTO FILMS_GENRES(FILM_ID,GENRE_ID) VALUES ";

        for (Genre genre : genres) {
            String extraQuery = "( %s , %s),".formatted(id, genre.getId());
            query = query + extraQuery;
        }
        query = query.substring(0, query.length() - 1);
        insertForTwoKeys(query);
    }
    @Override
    public void deleteFilmById(Integer id){

    }
}
