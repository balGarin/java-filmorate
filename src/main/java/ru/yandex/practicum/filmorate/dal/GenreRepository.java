package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class GenreRepository {
    private static final String FIND_ALL_GENRES = "SELECT * FROM GENRES";
    private static final String FIND_GENRE_BY_ID = "SELECT * FROM GENRES WHERE GENRE_ID = ?";

    private final JdbcTemplate jdbc;
    private final GenreRowMapper mapper;


    public List<Genre> getAllGenre() {
        return jdbc.query(FIND_ALL_GENRES, mapper);
    }

    public Genre getGenreById(Integer id) {
        try {
            return jdbc.queryForObject(FIND_GENRE_BY_ID, mapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Id жанра не найден!");
        }
    }
}
