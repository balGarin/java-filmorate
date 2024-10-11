package ru.yandex.practicum.filmorate.service;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.List;
import java.util.Optional;

@Service

public class FilmService {


    private final FilmStorage filmStorage;

    public FilmService(@Qualifier("DBFilms") FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film updateFilm(Film newFilm) {
        return filmStorage.updateFilm(newFilm);
    }

    public Film addFilm(Film newFilm) {
        return filmStorage.addFilm(newFilm);
    }

    public Film getFilmById(Integer id) {
        return filmStorage.getFilmById(id);
    }


    public void addLike(Integer id, Integer userId) {
        filmStorage.addLike(id, userId);
    }


    public void deleteLike(Integer id, Integer userId) {
        filmStorage.deleteLike(id, userId);
    }


    public List<Film> getMostPopularFilms(Integer count) {
        return filmStorage.getMostPopularFilms(count);
    }

    public void deleteFilmById(Integer id) {
        filmStorage.deleteFilmById(id);
    }

    /**
     * Вывод самых популярных фильмов по жанру и году или отдельно год, и отдельно жанр.
     *
     * @param count   количество топ фильмов, 10 по умолчанию,
     * @param genreId айди жанра, для фильтрации по жанру,
     * @param year    год выходы фильма, для фильтрации по году,
     * @return Возвращает список самых популярных фильмов указанного жанра за нужный год.
     */
    public List<Film> getMostPopular(Integer count, Optional<Integer> genreId, Optional<Integer> year) {
        if (genreId.isEmpty() && year.isEmpty()) {
            return filmStorage.getMostPopularFilms(count);
        } else if (year.isEmpty()) {
            return filmStorage.getPopularFilmsByGenre(count, genreId.get());
        } else if (genreId.isEmpty()) {
            return filmStorage.getPopularFilmsByYear(count, year.get());
        } else {
            return filmStorage.getPopularFilmsOnGenreAndYear(count, genreId.get(), year.get());
        }
    }

    /**
     * Вывод списка фильмов рекомендованных на основе лайков других пользователей
     *
     * @param userId полльзователя которму даются рекомендации
     * @return возврщает список фильмов
     */
    public List<Film> getRecommendations(Long userId) {
        return filmStorage.getRecommendations(userId);
    }

    public List<Film> getFilmsSortedByDirector(Integer directorId,String sortBy) {
        return filmStorage.getFilmsSortedByDirector(directorId,sortBy);
    }

    public List<Film> getCommonFilms(Integer userId, Integer friendId) {
        return filmStorage.getCommonFilms(userId, friendId);
    }
}
