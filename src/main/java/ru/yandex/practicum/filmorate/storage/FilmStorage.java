package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {

    Film addFilm(Film film);

    Film updateFilm(Film film);

    List<Film> getAllFilms();

    Film getFilmById(Integer id);

    void addLike(Integer id, Integer userId);

    void deleteLike(Integer id, Integer userId);

    List<Film> getMostPopularFilms(Integer count);

    void deleteFilmById(Integer id);

    /**
     * Вывод самых популярных фильмов по жанру и годам, 3 метода.
     *
     * @param count   количество топ фильмов, 10 по умолчанию,
     * @param genreId айди жанра, для фильтрации по жанру,
     * @param year    год выходы фильма, для фильтрации по году,
     * @return Возвращает список самых популярных фильмов указанного жанра за нужный год.
     */
    List<Film> getPopularFilmsOnGenreAndYear(Integer count, Integer genreId, Integer year);

    List<Film> getPopularFilmsByGenre(Integer count, Integer genreId);

    List<Film> getPopularFilmsByYear(Integer count, Integer year);

    /**
     * Вывод списка фильмов рекомендованных на основе лайков других пользователей
     *
     * @param userId полльзователя которму даются рекомендации
     * @return возврщает список фильмов
     */
    List<Film> getRecommendations(Long userId);
}
