package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.IncorrectDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;


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
        Film film = filmStorage.getFilmById(id);
        User user = userStorage.getById(userId);
        Set<Integer> likes = film.getLikes();
        if (likes.add(user.getId())) {
            log.info("Пользователь номер - {} поставил лайк фильму номер - {}", userId, id);
        } else {
            log.warn("Пользователь - {} пытался поставить фильму - {} лайк повторно", userId, id);
            throw new IncorrectDataException("Нельзя поставить лайк повторно");
        }
    }


    public void deleteLike(Integer id, Integer userId) {
        Film film = filmStorage.getFilmById(id);
        User user = userStorage.getById(userId);
        Set<Integer> likes = film.getLikes();
        if (!likes.remove(user.getId())) {
            log.warn("Лайк пользователя номер - {}не был найден для фильма номер - {}", userId, id);
            throw new NotFoundException("Лайк не найден");
        }
        log.info("Лайк пользователя номер - {} удален для фильма номер - {}", userId, id);


    }


    public List<Film> getMostPopularFilms(Integer count) {
        if (count < 0) {
            log.warn("Параметр count передан не верно - {}", count);
            throw new IncorrectDataException("Параметр count не может быть меньше 0");
        }
        List<Film> popularFilm = filmStorage.getAllFilms().stream()
                .sorted(Comparator.comparing(Film::getLikesSize).reversed())
                .limit(count)
                .collect(Collectors.toList());
        log.debug("Запрос на вызов популярных фильмов : {}", popularFilm);
        return popularFilm;
    }
}
