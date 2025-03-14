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

    public List<Film> getRecommendations(Long userId) {
        return filmStorage.getRecommendations(userId);
    }

    public List<Film> getFilmsSortedByDirector(Integer directorId, String sortBy) {
        return filmStorage.getFilmsSortedByDirector(directorId, sortBy);
    }

    public List<Film> getCommonFilms(Integer userId, Integer friendId) {
        return filmStorage.getCommonFilms(userId, friendId);
    }

    public List<Film> searchFilms(String query, List<String> by) {
        query = "%" + query + "%";
        query = query.toLowerCase();
        if ((by.size() == 2) && (by.contains("director")) && (by.contains("title"))) {
            return filmStorage.searchFilmByNameAndDirector(query);
        } else if ((by.size() == 1) && by.contains("director")) {
            return filmStorage.searchFilmByDirector(query);
        } else if ((by.size() == 1) && by.contains("title")) {
            return filmStorage.searchFilmByTitle(query);
        } else throw new IllegalArgumentException("Введены некорректные параметры");
    }
}
