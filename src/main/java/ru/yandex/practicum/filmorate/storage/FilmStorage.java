package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {

    Film addFilm(Film film);

    Film updateFilm(Film film);

    List<Film> getAllFilms();

    Film getFilmById(Integer id);

    void  addLike(Integer id, Integer userId);

     void deleteLike(Integer id, Integer userId);

     List<Film> getMostPopularFilms(Integer count);
}
