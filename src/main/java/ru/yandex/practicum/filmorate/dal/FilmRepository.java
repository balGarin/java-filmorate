package ru.yandex.practicum.filmorate.dal;

import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmSuperMapper;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.IncorrectDataException;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@Repository("DBFilms")
public class FilmRepository implements FilmStorage {

    private final JdbcTemplate jdbc;
    private final UserRowMapper userRowMapper;
    private final FilmSuperMapper filmSuperMapper;
    private final EventRepository eventRepository;

    private static final String ADD_FILM = "INSERT INTO FILMS (FILM_NAME,DESCRIPTION,RELEASEDATE,DURATION,RATING_ID)" +
            "VALUES(?,?,?,?,?)";

    private static final String GET_FILMS_SUPER = """
             SELECT F.FILM_ID,
                   F.FILM_NAME,
                   F.DESCRIPTION,
                   F.RELEASEDATE,
                   F.DURATION,
                   F.RATING_ID,
                   R.RATING_NAME,
                   LISTAGG(DISTINCT CONCAT(G.GENRE_ID, '/', G.GENRE_NAME)) FILTER (WHERE G.GENRE_ID IS NOT NULL) AS GENRES,
                   LISTAGG(DISTINCT FL.USER_ID) AS LIKES,
                   LISTAGG(DISTINCT CONCAT(D.DIRECTOR_ID, '/', D.DIRECTOR_NAME)) FILTER (WHERE D.DIRECTOR_ID IS NOT NULL) AS DIRECTORS
            FROM FILMS F
            LEFT JOIN RATINGS R ON F.RATING_ID = R.RATING_ID
            LEFT JOIN FILMS_GENRES FG ON F.FILM_ID = FG.FILM_ID
            LEFT JOIN GENRES G ON FG.GENRE_ID = G.GENRE_ID
            LEFT JOIN LIKES FL ON F.FILM_ID = FL.FILM_ID
            LEFT JOIN DIRECTORS_FILMS DF ON DF.FILM_ID = F.FILM_ID
            LEFT JOIN DIRECTORS D ON DF.DIRECTOR_ID = D.DIRECTOR_ID
            """;

    // Добавил рейтинг для теста Film update
    private static final String UPDATE_FILM = "UPDATE FILMS SET  FILM_NAME = ? , DESCRIPTION = ?," +
            "DURATION = ?, RELEASEDATE = ?, RATING_ID = ? " +
            "WHERE FILM_ID = ?";
    private static final String ADD_LIKE = "INSERT INTO LIKES (FILM_ID,USER_ID)" +
            "VALUES (?,?)";
    private static final String FIND_USER_BY_ID = "SELECT * FROM USERS " +
            "WHERE USER_ID = ?";
    private static final String DELETE_LIKE = "DELETE FROM LIKES " +
            "WHERE FILM_ID=? AND USER_ID=?";
    private static final String GET_POPULAR =
            """
                        SELECT F.FILM_ID,
                           F.FILM_NAME,
                           F.DESCRIPTION,
                           F.RELEASEDATE,
                           F.DURATION,
                           F.RATING_ID,
                           R.RATING_NAME,
                           LISTAGG(DISTINCT CONCAT(G.GENRE_ID, '/', G.GENRE_NAME))
                           FILTER (WHERE G.GENRE_ID IS NOT NULL) AS GENRES,
                           LISTAGG(DISTINCT FL.USER_ID) AS LIKES,
                           LISTAGG(DISTINCT CONCAT(D.DIRECTOR_ID, '/', D.DIRECTOR_NAME))
                           FILTER (WHERE D.DIRECTOR_ID IS NOT NULL) AS DIRECTORS
                    FROM FILMS F
                    LEFT JOIN RATINGS R ON F.RATING_ID = R.RATING_ID
                    LEFT JOIN FILMS_GENRES FG ON F.FILM_ID = FG.FILM_ID
                    LEFT JOIN GENRES G ON FG.GENRE_ID = G.GENRE_ID
                    LEFT JOIN LIKES FL ON F.FILM_ID = FL.FILM_ID
                    LEFT JOIN DIRECTORS_FILMS DF ON DF.FILM_ID = F.FILM_ID
                    LEFT JOIN DIRECTORS D ON D.DIRECTOR_ID = DF.DIRECTOR_ID
                         GROUP  BY f.FILM_ID
                         ORDER BY COUNT(FL.USER_ID) DESC
                         LIMIT  ?""";
    private static final String GET_LIST_OF_LIKES_BY_Id = "SELECT USER_ID FROM LIKES WHERE FILM_ID = ?";
    private static final String GET_POPULAR_FILM_ON_GENRES =
            GET_FILMS_SUPER + """
                    WHERE G.GENRE_ID = ?
                    GROUP BY f.FILM_ID
                    ORDER BY count(Fl.USER_ID) DESC
                    LIMIT ?""";
    private static final String GET_POPULAR_FILM_ON_YEAR =
            GET_FILMS_SUPER + """
                    WHERE EXTRACT(YEAR FROM CAST(f.RELEASEDATE AS date)) = ?
                    GROUP BY f.FILM_ID
                    ORDER BY count(Fl.USER_ID) DESC
                    LIMIT ?""";

    private static final String GET_POPULAR_FILMS_ON_GENRE_AND_YEAR =
            GET_FILMS_SUPER + """
                    WHERE EXTRACT(YEAR FROM CAST(F.RELEASEDATE AS date)) = ?
                    AND G.GENRE_ID = ?
                    GROUP BY f.FILM_ID
                    ORDER BY count(Fl.USER_ID) DESC
                    LIMIT ?""";
    private static final String DELETE_FILM_BY_ID = "DELETE FROM FILMS " +
            "WHERE FILM_ID=?";

    private static final String GET_RECOMMENDATIONS = GET_FILMS_SUPER +
            "WHERE f.FILM_ID IN (" +
            /* Поиск фильмов которые лайкнули другие пользователи, но не лайкнул пользователь */
            "SELECT FILM_ID FROM LIKES " +
            "WHERE USER_ID IN " +
            "(" +
            /* Поиск максимального совпадения по лайкам пользователя с другими пользователями */
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
            ")" +
            "GROUP BY F.FILM_ID";
    private static final String DELETE_CONNECTION_DIRECTORS_FILMS = "DELETE FROM DIRECTORS_FILMS " +
            "WHERE FILM_ID = ?";

    private static final String FIND_FILMS_BY_DIRECTOR_SORTED_BY_YEAR = """
            SELECT F.FILM_ID,
                    F.FILM_NAME,
                    F.DESCRIPTION,
                    F.RELEASEDATE,
                    F.DURATION,
                    F.RATING_ID,
                    R.RATING_NAME,
                    LISTAGG(DISTINCT CONCAT(G.GENRE_ID, '/', G.GENRE_NAME))
                    FILTER (WHERE G.GENRE_ID IS NOT NULL) AS GENRES,
                    LISTAGG(DISTINCT FL.USER_ID) AS LIKES,
                    LISTAGG(DISTINCT CONCAT(D.DIRECTOR_ID, '/', D.DIRECTOR_NAME))
                    FILTER (WHERE D.DIRECTOR_ID IS NOT NULL) AS DIRECTORS
             FROM FILMS F
             LEFT JOIN RATINGS R ON F.RATING_ID = R.RATING_ID
             LEFT JOIN FILMS_GENRES FG ON F.FILM_ID = FG.FILM_ID
             LEFT JOIN GENRES G ON FG.GENRE_ID = G.GENRE_ID
             LEFT JOIN LIKES FL ON F.FILM_ID = FL.FILM_ID
              JOIN DIRECTORS_FILMS DF ON DF.FILM_ID = F.FILM_ID
              JOIN DIRECTORS D ON D.DIRECTOR_ID = DF.DIRECTOR_ID
              WHERE D.DIRECTOR_ID = ?
              GROUP  BY F.FILM_ID
              ORDER BY f.RELEASEDATE ASC""";
    private static final String FIND_FILMS_BY_DIRECTOR_SORTED_BY_LIKES =
            GET_FILMS_SUPER + """
                    WHERE d.DIRECTOR_ID = ?
                     GROUP  BY f.FILM_ID
                     ORDER BY COUNT(FL.USER_ID) DESC""";

    private static final String FIND_COMMON_FILMS_SORTED_BY_LIKES =
            GET_FILMS_SUPER + """
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
                              ORDER BY COUNT(DISTINCT FL.USER_ID) DESC""";

    private static final String SEARCH_FILM_BY_TITLE =
            GET_FILMS_SUPER + """
                    WHERE F.FILM_NAME LIKE ?
                    GROUP BY f.FILM_ID ORDER BY COUNT( Fl.FILM_ID ) DESC""";
    private static final String SEARCH_FILM_BY_DIRECTOR =
            GET_FILMS_SUPER + """
                    WHERE D.DIRECTOR_NAME LIKE ?
                    GROUP BY f.FILM_ID ORDER BY COUNT( Fl.FILM_ID ) DESC""";
    private static final String SEARCH_FILM_BY_DIRECTOR_AND_TITLE =
            GET_FILMS_SUPER + """
                    WHERE (d.DIRECTOR_NAME LIKE ? OR  f.FILM_NAME LIKE ?)
                    GROUP BY f.FILM_ID ORDER BY COUNT( Fl.FILM_ID ) desc""";


    private static final String DELETE_CONNECTION_FILMS_GENRES = "DELETE FROM FILMS_GENRES WHERE FILM_ID = ?";


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
        if (newFilm.getId() == null) {
            throw new IncorrectDataException("Поле ID обязательно для этой операции");
        }
        // Добавил рейтинг для теста Film update
        int rowChanged = jdbc.update(UPDATE_FILM, newFilm.getName(), newFilm.getDescription(), newFilm.getDuration(),
                newFilm.getReleaseDate(), newFilm.getMpa().getId(), newFilm.getId());
        if (rowChanged == 0) {
            throw new NotFoundException("Не удалось обновить данные");
        }
        Set<Director> directors = newFilm.getDirectors();
        jdbc.update(DELETE_CONNECTION_DIRECTORS_FILMS, newFilm.getId());
        addDirectors(newFilm.getId(), directors);
        Set<Genre> genres = newFilm.getGenres();
        jdbc.update(DELETE_CONNECTION_FILMS_GENRES, newFilm.getId());
        addGenres(newFilm.getId(), genres);
        Film film = jdbc.queryForObject(GET_FILMS_SUPER + " WHERE F.FILM_ID = ?", filmSuperMapper, newFilm.getId());
        return film;
    }

    @Override
    public List<Film> getAllFilms() {
        return jdbc.query(GET_FILMS_SUPER + " GROUP BY F.FILM_ID", filmSuperMapper);
    }

    @Override
    public Film getFilmById(Integer id) {
        // Добавил проверку FILM_ID для теста Film id=9999 get not found
        String sql = "SELECT EXISTS(SELECT 1 FROM films WHERE film_id = ?);";
        if (Boolean.FALSE.equals(jdbc.queryForObject(sql, Boolean.class, id))) {
            throw new NotFoundException("Должен быть указан существующий id");
        }
            return jdbc.queryForObject(GET_FILMS_SUPER + " WHERE F.FILM_ID = ?", filmSuperMapper, id);
    }

    @Override
    public void addLike(Integer id, Integer userId) {
        try {
            User user = jdbc.queryForObject(FIND_USER_BY_ID, userRowMapper, userId);
        } catch (DataAccessException e) {
            throw new NotFoundException("Пользователя с " + userId + " ID не найден!");
        }
        try {
            Film film = jdbc.queryForObject(GET_FILMS_SUPER + " WHERE F.FILM_ID = ?", filmSuperMapper, id);
        } catch (DataAccessException e) {
            throw new NotFoundException("Фильм с " + id + " ID не найден!");
        }
        insertForTwoKeys(ADD_LIKE, id, userId);
        eventRepository.addEvent(Event.builder()
                .userId(userId)
                .eventType(TypeOfEvent.LIKE)
                .operation(OperationType.ADD)
                .timestamp(Instant.now().toEpochMilli())
                .entityId(id)
                .build());
    }

    @Override
    public void deleteLike(Integer id, Integer userId) {
        try {
            User user = jdbc.queryForObject(FIND_USER_BY_ID, userRowMapper, userId);
        } catch (DataAccessException e) {
            throw new NotFoundException("Пользователя с " + userId + " ID не найден!");
        }
        try {
            Film film = jdbc.queryForObject(GET_FILMS_SUPER + " WHERE F.FILM_ID = ?", filmSuperMapper, id);
        } catch (DataAccessException e) {
            throw new NotFoundException("Фильм с " + id + " ID не найден!");
        }
        int rowsDeleted = jdbc.update(DELETE_LIKE, id, userId);
        if (rowsDeleted == 0) {
            throw new NotFoundException(" Лайк не найден");
        }
        eventRepository.addEvent(Event.builder()
                .userId(userId)
                .eventType(TypeOfEvent.LIKE)
                .operation(OperationType.REMOVE)
                .timestamp(Instant.now().toEpochMilli())
                .entityId(id)
                .build());
    }

    @Override
    public List<Film> getMostPopularFilms(Integer count) {
        List<Film> films = jdbc.query(GET_POPULAR, filmSuperMapper, count);
        return films;
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
        return jdbc.query(GET_POPULAR_FILMS_ON_GENRE_AND_YEAR, filmSuperMapper, year, genreId, count);
    }

    @Override
    public List<Film> getPopularFilmsByGenre(Integer count, Integer genreId) {
        return jdbc.query(GET_POPULAR_FILM_ON_GENRES, filmSuperMapper, genreId, count);
    }

    @Override
    public List<Film> getPopularFilmsByYear(Integer count, Integer year) {
        return jdbc.query(GET_POPULAR_FILM_ON_YEAR, filmSuperMapper, year, count);
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

    public List<Integer> getLikes(Integer id) {
        return jdbc.queryForList(GET_LIST_OF_LIKES_BY_Id, Integer.class, id);
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
        List<Film> films = jdbc.query(GET_RECOMMENDATIONS, filmSuperMapper, userId, userId, userId);
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
        // Добавил проверку DIRECTOR_ID для теста Get films with deleted director
        String sql = "SELECT EXISTS(SELECT 1 FROM DIRECTORS WHERE DIRECTOR_ID = ?);";
        if (Boolean.FALSE.equals(jdbc.queryForObject(sql, Boolean.class, directorId))) {
            throw new NotFoundException("Должен быть указан существующий id");
        }
        List<Film> films;
        if (sortBy.equals("year")) {
            films = jdbc.query(FIND_FILMS_BY_DIRECTOR_SORTED_BY_YEAR, filmSuperMapper, directorId);
        } else if (sortBy.equals("likes")) {
            films = jdbc.query(FIND_FILMS_BY_DIRECTOR_SORTED_BY_LIKES, filmSuperMapper, directorId);
        } else {
            throw new IncorrectDataException("Не верный запрос");
        }
        return films;
    }

    @Override
    public List<Film> getCommonFilms(Integer userId, Integer friendId) {
        List<Film> films;
        films = jdbc.query(FIND_COMMON_FILMS_SORTED_BY_LIKES, filmSuperMapper, userId, friendId);
        return films;
    }

    @Override
    public List<Film> searchFilmByDirector(String query) {
        List<Film> films = jdbc.query(SEARCH_FILM_BY_DIRECTOR, filmSuperMapper, query);
        return films;
    }

    @Override
    public List<Film> searchFilmByTitle(String query) {
        List<Film> films = jdbc.query(SEARCH_FILM_BY_TITLE, filmSuperMapper, query);
        return films;
    }

    @Override
    public List<Film> searchFilmByNameAndDirector(String query) {
        try {
            List<Film> films = jdbc.query(SEARCH_FILM_BY_DIRECTOR_AND_TITLE, filmSuperMapper, query, query);
            return films;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
