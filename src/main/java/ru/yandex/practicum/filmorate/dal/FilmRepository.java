package ru.yandex.practicum.filmorate.dal;

import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.*;
import ru.yandex.practicum.filmorate.exception.IncorrectDataException;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private final DirectorRowMapper directorRowMapper;
    private final FilmRowMapperWithDir filmRowMapperWithDir;
    private final FilmRowMapperCommon filmRowMapperCommon;

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

    private static final String FIND_DIRECTORS_BY_FILM_ID = "SELECT d.DIRECTOR_ID, d.DIRECTOR_NAME " +
            " FROM DIRECTORS_FILMS df " +
            "JOIN DIRECTORS d ON d.DIRECTOR_ID = df.DIRECTOR_ID WHERE df.FILM_ID = ?";


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
            "LEFT JOIN LIKES l ON f.FILM_ID =l.FILM_ID " +
            "GROUP  BY f.FILM_ID  " +
            "ORDER BY COUNT(l.USER_ID) DESC " +
            "LIMIT ?";

    private static final String GET_LIST_OF_LIKES_BY_Id = "SELECT USER_ID FROM LIKES WHERE FILM_ID = ?";
    private static final String GET_ALL_FILMS_WITH_ALL_FIELDS = "SELECT f.FILM_ID,f.FILM_NAME,f.DESCRIPTION," +
            "f.RELEASEDATE,f.DURATION,f.RATING_ID,r.RATING_NAME,g.GENRE_ID,g.GENRE_NAME, d.DIRECTOR_ID, d.DIRECTOR_NAME " +
            "FROM FILMS f " +
            "LEFT JOIN FILMS_GENRES fg ON f.FILM_ID = fg.FILM_ID " +
            "LEFT JOIN RATINGS r ON f.RATING_ID =r.RATING_ID " +
            "LEFT JOIN GENRES g ON fg.GENRE_ID = g.GENRE_ID " +
            "LEFT JOIN DIRECTORS_FILMS df ON f.FILM_ID = df.FILM_ID " +
            "LEFT JOIN DIRECTORS d ON df.DIRECTOR_ID = d.DIRECTOR_ID ";

    private static final String GET_POPULAR_FILM_ON_GENRES = GET_ALL_FILMS_WITH_ALL_FIELDS +
            "WHERE f.FILM_ID IN (" +
            "SELECT id FROM (" +
            "SELECT f.FILM_ID id, l.USER_ID " +
            "FROM FILMS f " +
            "JOIN FILMS_GENRES fg ON f.FILM_ID = fg.FILM_ID " +
            "JOIN GENRES g ON fg.GENRE_ID = g.GENRE_ID " +
            "LEFT JOIN LIKES l ON f.FILM_ID = l.FILM_ID " +
            "WHERE g.GENRE_ID = ? " +
            "GROUP BY f.FILM_ID " +
            "ORDER BY count(l.USER_ID) DESC " +
            "LIMIT ?) " +
            ")";

    private static final String GET_POPULAR_FILM_ON_YEAR = GET_ALL_FILMS_WITH_ALL_FIELDS +
            "WHERE f.FILM_ID IN (" +
            "SELECT id FROM (" +
            "SELECT f.FILM_ID id, l.USER_ID " +
            "FROM FILMS f " +
            "JOIN FILMS_GENRES fg ON f.FILM_ID = fg.FILM_ID " +
            "JOIN GENRES g ON fg.GENRE_ID = g.GENRE_ID " +
            "LEFT JOIN LIKES l ON f.FILM_ID = l.FILM_ID " +
            "WHERE EXTRACT(YEAR FROM CAST(f.RELEASEDATE AS date)) = ? " +
            "GROUP BY f.FILM_ID " +
            "ORDER BY count(l.USER_ID) DESC " +
            "LIMIT ?) " +
            ")";

    private static final String GET_POPULAR_FILMS_ON_GENRE_AND_YEAR = GET_ALL_FILMS_WITH_ALL_FIELDS +
            "WHERE f.FILM_ID IN (" +
            "SELECT id FROM (" +
            "SELECT f.FILM_ID id, l.USER_ID " +
            "FROM FILMS f " +
            "JOIN FILMS_GENRES fg ON f.FILM_ID = fg.FILM_ID " +
            "JOIN GENRES g ON fg.GENRE_ID = g.GENRE_ID " +
            "LEFT JOIN LIKES l ON f.FILM_ID = l.FILM_ID " +
            "WHERE EXTRACT(YEAR FROM CAST(f.RELEASEDATE AS date)) = ? " +
            "AND g.GENRE_ID = ? " +
            "GROUP BY f.FILM_ID " +
            "ORDER BY count(l.USER_ID) DESC " +
            "LIMIT ?) " +
            ")";

    private static final String DELETE_FILM_BY_ID = "DELETE FROM FILMS " +
            "WHERE FILM_ID=?";

    private static final String GET_RECOMMENDATIONS = GET_ALL_FILMS_WITH_ALL_FIELDS +
            "WHERE f.FILM_ID IN (" +
            /* Поиск фильмов которые лайкнули другие пользователи, но не лайкнул пользователь */
            "SELECT FILM_ID FROM LIKES " +
            "WHERE USER_ID IN " +
            "(" +
            /* Поиск максимального совпадений по лайкам пользователя с другими пользователями */
            "SELECT likes.USER_ID FROM LIKES likes " +
            "RIGHT JOIN LIKES likesUser ON likesUser.FiLM_ID = likes.FiLM_ID " +
            "GROUP BY likes.USER_ID, likesUser.USER_ID " +
            "HAVING likes.USER_ID IS NOT NULL " +
            "AND likes.USER_ID != ? " +
            "AND likesUser.USER_ID = ? " +
            "ORDER BY count(likes.USER_ID) DESC " +
            "LIMIT 10 " +
            ") " +
            /* Поиск фильмов которые не лайкал(не посмотрел) пользователь */
            "AND FILM_ID NOT IN " +
            "(" +
            "SELECT FILM_ID FROM LIKES " +
            "WHERE USER_ID = ? " +
            ") " +
            ")";

    private static final String DELETE_CONNECTION_DIRECTORS_FILMS = "DELETE FROM DIRECTORS_FILMS " +
            "WHERE FILM_ID = ?";

    private static final String FIND_FILMS_BY_DIRECTOR_SORTED_BY_YEAR = "SELECT f.FILM_ID,f.FILM_NAME," +
            "f.DESCRIPTION,f.RELEASEDATE,f.DURATION ,r.RATING_ID ,r.RATING_NAME ," +
            "d.DIRECTOR_ID,d.DIRECTOR_NAME FROM FILMS f " +
            "JOIN RATINGS r ON r.RATING_ID =f.RATING_ID " +
            " JOIN DIRECTORS_FILMS df ON f.FILM_ID =DF.FILM_ID " +
            " JOIN DIRECTORS d ON d.DIRECTOR_ID =df.DIRECTOR_ID " +
            " WHERE d.DIRECTOR_ID = ? " +
            " ORDER BY f.RELEASEDATE ASC ";

    private static final String FIND_FILMS_BY_DIRECTOR_SORTED_BY_LIKES = "SELECT f.FILM_ID,f.FILM_NAME," +
            "f.DESCRIPTION,f.RELEASEDATE,f.DURATION ,r.RATING_ID ,r.RATING_NAME ," +
            "d.DIRECTOR_ID,d.DIRECTOR_NAME FROM FILMS f " +
            "LEFT JOIN LIKES l ON f.FILM_ID =l.FILM_ID " +
            "JOIN RATINGS r ON r.RATING_ID =f.RATING_ID " +
            "JOIN DIRECTORS_FILMS df ON df.FILM_ID = f.FILM_ID " +
            "JOIN DIRECTORS d ON d.DIRECTOR_ID = df.DIRECTOR_ID " +
            "WHERE d.DIRECTOR_ID = ? " +
            "GROUP  BY f.FILM_ID  " +
            "ORDER BY COUNT(l.USER_ID) DESC ";

    private static final String FIND_COMMON_FILMS_SORTED_BY_LIKES = """
            SELECT F.FILM_ID,
                   F.FILM_NAME,
                   F.DESCRIPTION,
                   F.RELEASEDATE,
                   F.DURATION,
                   F.RATING_ID,
                   R.RATING_NAME,
                   LISTAGG(DISTINCT CONCAT(G.GENRE_ID, '/', G.GENRE_NAME)) FILTER (WHERE G.GENRE_ID IS NOT NULL) AS GENRES,
                   LISTAGG(DISTINCT FL.USER_ID) AS LIKES
            FROM FILMS F
            LEFT JOIN RATINGS R ON F.RATING_ID = R.RATING_ID
            LEFT JOIN FILMS_GENRES FG ON F.FILM_ID = FG.FILM_ID
            LEFT JOIN GENRES G ON FG.GENRE_ID = G.GENRE_ID
            LEFT JOIN LIKES FL ON F.FILM_ID = FL.FILM_ID
            WHERE F.FILM_ID IN (
                SELECT FILM_ID,
                FROM LIKES l
                WHERE USER_ID = ? OR USER_ID = ?
                GROUP BY FILM_ID
                HAVING COUNT(*) = 2
            )
            GROUP BY F.FILM_ID,
                     F.FILM_NAME,
                     F.DESCRIPTION,
                     F.RELEASEDATE,
                     F.DURATION,
                     F.RATING_ID,
                     R.RATING_NAME
            ORDER BY COUNT(DISTINCT FL.USER_ID) DESC
            """;

    private static final String SEARCH_FILM_BY_TITLE = GET_ALL_FILMS_WITH_ALL_FIELDS +
            " LEFT JOIN LIKES l ON f.FILM_ID = l.FILM_ID " +
            " WHERE f.FILM_NAME LIKE ? " +
            " GROUP BY f.FILM_ID ORDER BY COUNT( l.FILM_ID ) desc";
    private static final String SEARCH_FILM_BY_DIRECTOR = GET_ALL_FILMS_WITH_ALL_FIELDS +
            " LEFT JOIN LIKES l ON f.FILM_ID = l.FILM_ID " +
            " WHERE d.DIRECTOR_NAME LIKE ? " +
            " GROUP BY f.FILM_ID ORDER BY COUNT( l.FILM_ID ) desc";
    private static final String SEARCH_FILM_BY_DIRECTOR_AND_TITLE = GET_ALL_FILMS_WITH_ALL_FIELDS +
            " LEFT JOIN LIKES l ON f.FILM_ID = l.FILM_ID " +
            " WHERE (d.DIRECTOR_NAME LIKE ? OR  f.FILM_NAME LIKE ?) " +
            " GROUP BY f.FILM_ID ORDER BY COUNT( l.FILM_ID ) desc";

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
        if (film.getDirectors() == null) {
            film.setDirectors(new HashSet<>());
        }
        Integer id = insert(ADD_FILM, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(),
                film.getMpa().getId());
        film.setId(id);
        Set<Genre> genres = film.getGenres();
        Set<Director> directors = film.getDirectors();
        addGenres(id, genres);
        addDirectors(id, directors);
        return film;

    }

    @Override
    public Film updateFilm(Film newFilm) {
        try {
            jdbc.queryForObject(FIND_FILM_BY_ID, filmRowMapper, newFilm.getId());

        } catch (DataAccessException e) {
            throw new NotFoundException("Пользователь с таким ID не найден");
        }

        jdbc.update(UPDATE_FILM, newFilm.getId(), newFilm.getName(), newFilm.getDescription(), newFilm.getDuration(),
                newFilm.getReleaseDate(), newFilm.getId());
        Set<Director> directors = newFilm.getDirectors();
        jdbc.update(DELETE_CONNECTION_DIRECTORS_FILMS, newFilm.getId());
        addDirectors(newFilm.getId(), directors);
        Film film = jdbc.queryForObject(FIND_FILM_BY_ID, filmRowMapper, newFilm.getId());
        film.setDirectors(directors);
        return film;
    }

    @Override
    public List<Film> getAllFilms() {
        List<Film> films = jdbc.query(FIND_ALL_FILMS, filmRowMapper);
        return fellFilms(films);
    }

    @Override
    public Film getFilmById(Integer id) {
        MPA mpa = new MPA();
        try {
            Film film = jdbc.queryForObject(FIND_FILM_BY_ID, filmRowMapper, id);
            if (film.getMpa() != null) {
                mpa = jdbc.queryForObject(FIND_MPA_BY_ID, mpaRowMapper, film.getMpa().getId());
            }
            film.setMpa(mpa);
            List<Genre> genres = jdbc.query(FIND_GENRE_BY_FILM_ID, genreRowMapper, film.getId());
            List<Director> directors = jdbc.query(FIND_DIRECTORS_BY_FILM_ID, directorRowMapper, film.getId());
            List<Integer> likes = jdbc.query(GET_LIST_OF_LIKES_BY_Id, likeRowMapper, film.getId());
            film.setLikes(new HashSet<>(likes));
            film.setGenres(new HashSet<>(genres));
            film.setDirectors(new HashSet<>(directors));
            return film;
        } catch (DataAccessException e) {
            throw new NotFoundException("Фильм c ID " + id + " не найден");
        }


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

    /**
     * Вывод самых популярных фильмов по жанру и годам, 3 метода.
     *
     * @param count   количество топ фильмов, 10 по умолчанию,
     * @param genreId айди жанра, для фильтрации по жанру,
     * @param year    год выходы фильма, для фильтрации по году,
     * @return Возвращает список самых популярных фильмов указанного жанра за нужный год.
     */
    @Override
    public List<Film> getPopularFilmsOnGenreAndYear(Integer count, Integer genreId, Integer year) {
        List<Film> films = jdbc.query(GET_POPULAR_FILMS_ON_GENRE_AND_YEAR, filmFullRowMapper, year, genreId, count);
        return films;
    }

    @Override
    public List<Film> getPopularFilmsByGenre(Integer count, Integer genreId) {
        List<Film> films = jdbc.query(GET_POPULAR_FILM_ON_GENRES, filmFullRowMapper, genreId, count);
        return films;
    }

    @Override
    public List<Film> getPopularFilmsByYear(Integer count, Integer year) {
        List<Film> films = jdbc.query(GET_POPULAR_FILM_ON_YEAR, filmFullRowMapper, year, count);
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
    public void deleteFilmById(Integer id) {
        int rowDeleted = jdbc.update(DELETE_FILM_BY_ID, id);
        if (rowDeleted == 0) {
            throw new NotFoundException("Фильм не найден");
        }
    }

    /**
     * Вывод списка фильмов рекомендованных на основе лайков других пользователей
     *
     * @param userId полльзователя которму даются рекомендации
     * @return возврщает список фильмов
     */
    @Override
    public List<Film> getRecommendations(Long userId) {
        List<Film> films = jdbc.query(GET_RECOMMENDATIONS, filmFullRowMapper, userId, userId, userId);
        return films;
    }

    private void addDirectors(Integer id, Set<Director> directors) {
        if (directors == null || directors.size() == 0) {
            return;
        }
        String query = "INSERT INTO DIRECTORS_FILMS (FILM_ID,DIRECTOR_ID) VALUES ";
        for (Director director : directors) {
            String extraQuery = "( %s , %s),".formatted(id, director.getId());
            query = query + extraQuery;
        }
        query = query.substring(0, query.length() - 1);
        insertForTwoKeys(query);
    }

    @Override
    public List<Film> getFilmsSortedByDirector(Integer directorId, String sortBy) {
        List<Film> films;
        if (sortBy.equals("year")) {
            films = jdbc.query(FIND_FILMS_BY_DIRECTOR_SORTED_BY_YEAR, filmRowMapperWithDir, directorId);
        } else if (sortBy.equals("likes")) {
            films = jdbc.query(FIND_FILMS_BY_DIRECTOR_SORTED_BY_LIKES, filmRowMapperWithDir, directorId);
        } else {
            throw new IncorrectDataException("Не верный запрос");
        }
        return films;
    }

    @Override
    public List<Film> getCommonFilms(Integer userId, Integer friendId) {
        List<Film> films;
        films = jdbc.query(FIND_COMMON_FILMS_SORTED_BY_LIKES, filmRowMapperCommon, userId, friendId);
        return films;
    }

    @Override
    public List<Film> searchFilmByDirector(String query) {
        List<Film> films = jdbc.query(SEARCH_FILM_BY_DIRECTOR, filmFullRowMapper, query);
        return films;
    }

    @Override
    public List<Film> searchFilmByTitle(String query) {
        List<Film> films = jdbc.query(SEARCH_FILM_BY_TITLE, filmFullRowMapper, query);
        return films;
    }

    @Override
    public List<Film> searchFilmByNameAndDirector(String query) {
        try {
            List<Film> films = jdbc.query(SEARCH_FILM_BY_DIRECTOR_AND_TITLE, filmFullRowMapper, query, query);
            return films;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
