package ru.yandex.practicum.filmorate.service;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.List;

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
}
