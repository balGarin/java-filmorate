package ru.yandex.practicum.filmorate.dal;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
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
import java.util.*;

@AllArgsConstructor
@Repository("DBFilms")
@Slf4j
public class FilmRepository implements FilmStorage {

    private final JdbcTemplate jdbc;
    private final UserRowMapper userRowMapper;
    private final FilmRowMapper filmRowMapper;
    private final EventRepository eventRepository;

    private static final String ADD_FILM = "INSERT INTO FILMS (FILM_NAME,DESCRIPTION,RELEASEDATE,DURATION,RATING_ID)" +
            "VALUES(?,?,?,?,?)";

    private static final String GET_FILMS = """
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

    private static final String GET_POPULAR_FILM_ON_YEAR =
            GET_FILMS + """
                    WHERE EXTRACT(YEAR FROM CAST(f.RELEASEDATE AS date)) = ?
                    GROUP BY f.FILM_ID
                    ORDER BY count(Fl.USER_ID) DESC
                    LIMIT ?""";

    private static final String DELETE_FILM_BY_ID = "DELETE FROM FILMS " +
            "WHERE FILM_ID=?";

    private static final String GET_RECOMMENDATIONS = GET_FILMS +
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
            GET_FILMS + """
                    WHERE d.DIRECTOR_ID = ?
                     GROUP  BY f.FILM_ID
                     ORDER BY COUNT(FL.USER_ID) DESC""";

    private static final String FIND_COMMON_FILMS_SORTED_BY_LIKES =
            GET_FILMS + """
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
            GET_FILMS + """
                    WHERE LOWER(F.FILM_NAME) LIKE ?
                    GROUP BY f.FILM_ID ORDER BY COUNT( Fl.FILM_ID ) DESC""";
    private static final String SEARCH_FILM_BY_DIRECTOR =
            GET_FILMS + """
                    WHERE LOWER(D.DIRECTOR_NAME) LIKE ?
                    GROUP BY f.FILM_ID ORDER BY COUNT( Fl.FILM_ID ) DESC""";
    private static final String SEARCH_FILM_BY_DIRECTOR_AND_TITLE =
            GET_FILMS + """
                    WHERE (LOWER(d.DIRECTOR_NAME)LIKE ? OR  LOWER(f.FILM_NAME)LIKE ?)
                    GROUP BY f.FILM_ID ORDER BY COUNT( Fl.FILM_ID )""";

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
        System.out.println(id);
        film.setId(id);
        System.out.println(film.getGenres());
        addGenres(id, film.getGenres());
        addDirectors(id, film.getDirectors());
        return jdbc.queryForObject(GET_FILMS + " WHERE F.FILM_ID=?", filmRowMapper, id);
    }

    @Override
    public Film updateFilm(Film newFilm) {
        if (newFilm.getId() == null) {
            throw new IncorrectDataException("Поле ID обязательно для этой операции");
        }
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
        Film film = jdbc.queryForObject(GET_FILMS + " WHERE F.FILM_ID = ?", filmRowMapper, newFilm.getId());
        return film;
    }

    @Override
    public List<Film> getAllFilms() {
        return jdbc.query(GET_FILMS + " GROUP BY F.FILM_ID", filmRowMapper);
    }

    @Override
    public Film getFilmById(Integer id) {
        String sql = "SELECT EXISTS(SELECT 1 FROM films WHERE film_id = ?);";
        if (Boolean.FALSE.equals(jdbc.queryForObject(sql, Boolean.class, id))) {
            throw new NotFoundException("Должен быть указан существующий id");
        }
        return jdbc.queryForObject(GET_FILMS + " WHERE F.FILM_ID = ?", filmRowMapper, id);
    }

    @Override
    public void addLike(Integer id, Integer userId) {
        try {
            User user = jdbc.queryForObject(FIND_USER_BY_ID, userRowMapper, userId);
        } catch (DataAccessException e) {
            throw new NotFoundException("Пользователя с " + userId + " ID не найден!");
        }
        try {
            Film film = jdbc.queryForObject(GET_FILMS + " WHERE F.FILM_ID = ?", filmRowMapper, id);
        } catch (DataAccessException e) {
            throw new NotFoundException("Фильм с " + id + " ID не найден!");
        }
        jdbc.update(DELETE_LIKE, id, userId);
        insertForTwoKeys(ADD_LIKE, id, userId);
        eventRepository.addEvent(Event.builder()
                .userId(userId)
                .eventType(TypeOfEvent.LIKE)
                .operation(OperationType.ADD)
                .timestamp(Instant.now().toEpochMilli())
                .entityId(id)
                .build());
        log.warn("{} поставил лайк {}", userId, id);
    }

    @Override
    public void deleteLike(Integer id, Integer userId) {
        try {
            User user = jdbc.queryForObject(FIND_USER_BY_ID, userRowMapper, userId);
        } catch (DataAccessException e) {
            throw new NotFoundException("Пользователя с " + userId + " ID не найден!");
        }
        try {
            Film film = jdbc.queryForObject(GET_FILMS + " WHERE F.FILM_ID = ?", filmRowMapper, id);
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
        log.warn("{} удалил лайк у {}", userId, id);
    }

    @Override
    public List<Film> getMostPopularFilms(Integer count) {
        List<Film> films = jdbc.query(GET_POPULAR, filmRowMapper, count);
        return films;
    }

    @Override
    public List<Film> getPopularFilmsOnGenreAndYear(Integer count, Integer genreId, Integer year) {
        List<Film> onYearFilm = jdbc.query(GET_FILMS + " WHERE EXTRACT(YEAR FROM CAST(F.RELEASEDATE AS date)) = ?" +
                        " GROUP BY F.FILM_ID",
                filmRowMapper, year);
        List<Film> onGenreFilms = new ArrayList<>();
        for (Film film : onYearFilm) {
            for (Genre genre : film.getGenres()) {
                if (genre.getId().equals(genreId)) {
                    onGenreFilms.add(film);
                }
            }
        }
        return onGenreFilms;
    }

    @Override
    public List<Film> getPopularFilmsByGenre(Integer count, Integer genreId) {
        List<Film> allFilms = jdbc.query(GET_FILMS + " GROUP BY F.FILM_ID ", filmRowMapper);
        List<Film> onGenreFilms = new ArrayList<>();
        for (Film film : allFilms) {
            for (Genre genre : film.getGenres()) {
                if (genre.getId().equals(genreId)) {
                    onGenreFilms.add(film);
                }
            }
        }
        return onGenreFilms;
    }

    @Override
    public List<Film> getPopularFilmsByYear(Integer count, Integer year) {
        return jdbc.query(GET_POPULAR_FILM_ON_YEAR, filmRowMapper, year, count);
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

    @Override
    public List<Film> getRecommendations(Long userId) {
        List<Film> films = jdbc.query(GET_RECOMMENDATIONS, filmRowMapper, userId, userId, userId);
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
        String sql = "SELECT EXISTS(SELECT 1 FROM DIRECTORS WHERE DIRECTOR_ID = ?);";
        if (Boolean.FALSE.equals(jdbc.queryForObject(sql, Boolean.class, directorId))) {
            throw new NotFoundException("Должен быть указан существующий id");
        }
        List<Film> films;
        if (sortBy.equals("year")) {
            films = jdbc.query(FIND_FILMS_BY_DIRECTOR_SORTED_BY_YEAR, filmRowMapper, directorId);
        } else if (sortBy.equals("likes")) {
            films = jdbc.query(FIND_FILMS_BY_DIRECTOR_SORTED_BY_LIKES, filmRowMapper, directorId);
        } else {
            throw new IncorrectDataException("Не верный запрос");
        }
        return films;
    }

    @Override
    public List<Film> getCommonFilms(Integer userId, Integer friendId) {
        List<Film> films;
        films = jdbc.query(FIND_COMMON_FILMS_SORTED_BY_LIKES, filmRowMapper, userId, friendId);
        return films;
    }

    @Override
    public List<Film> searchFilmByDirector(String query) {
        List<Film> films = jdbc.query(SEARCH_FILM_BY_DIRECTOR, filmRowMapper, query);
        return films;
    }

    @Override
    public List<Film> searchFilmByTitle(String query) {
        List<Film> films = jdbc.query(SEARCH_FILM_BY_TITLE, filmRowMapper, query);
        return films;
    }

    @Override
    public List<Film> searchFilmByNameAndDirector(String query) {
        try {
            List<Film> films = jdbc.query(SEARCH_FILM_BY_DIRECTOR_AND_TITLE, filmRowMapper, query, query);
            return films.stream().sorted(Comparator.comparing(Film::getId).reversed()).toList();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
