package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.IncorrectDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Repository("InMemoryFilms")
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private Map<Integer, Film> films = new HashMap<>();
    private InMemoryUserStorage userStorage ;
    public InMemoryFilmStorage(InMemoryUserStorage userStorage){
        this.userStorage=userStorage;
    }

    @Override
    public Film addFilm(Film newFilm) {
        log.debug("Валидация успешно пройдена!");
        newFilm.setId(generateId());
        newFilm.setLikes(new HashSet<>());
        films.put(newFilm.getId(), newFilm);
        log.info("Добавлен новый фильм {}", newFilm.toString());
        return newFilm;
    }

    @Override
    public Film updateFilm(Film newFilm) {
        log.debug("Валидация успешно пройдена!");
        log.info("Новый фильм для обновления {}", newFilm.toString());
        if (newFilm.getId() == null) {
            log.warn("Ошибка валидации : не указан id");
            throw new IncorrectDataException("Ошибка валидации : Не укажите id");
        }
        if (!films.containsKey(newFilm.getId())) {
            log.warn("Ошибка валидации : указан некорректный id");
            throw new NotFoundException("Ошибка валидации : указан некорректный id");

        }
        Film film = films.get(newFilm.getId());
        film.setName(newFilm.getName());
        if (newFilm.getDescription() != null) {
            film.setDescription(newFilm.getDescription());
        }
        if (newFilm.getDuration() != null) {
            film.setDuration(newFilm.getDuration());
        }
        if (newFilm.getReleaseDate() != null) {
            film.setReleaseDate(newFilm.getReleaseDate());
        }
        log.info("Фильм № {} успешно обновлен ", film.toString());
        return film;
    }

    @Override
    public Film getFilmById(Integer id) {
        if (!films.containsKey(id)) {
            log.warn("Фильм номер - {} не найден ", id);
            throw new NotFoundException("Такого фильма нет");
        }
        log.debug("Запрос на получение фильма номер - {}", id);
        return films.get(id);
    }

    @Override
    public List<Film> getAllFilms() {
        log.debug("Запрос на вывод всех фильмов {}", films.values());
        return new ArrayList<>(films.values());
    }

    private int generateId() {
        int id = films.keySet().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
        log.debug("Новый id - {}", id);
        return ++id;
    }
@Override
    public void addLike(Integer id, Integer userId) {
        Film film = getFilmById(id);
        User user = userStorage.getById(userId);
        Set<Integer> likes = film.getLikes();
        if (likes.add(user.getId())) {
            log.info("Пользователь номер - {} поставил лайк фильму номер - {}", userId, id);
        } else {
            log.warn("Пользователь - {} пытался поставить фильму - {} лайк повторно", userId, id);
            throw new IncorrectDataException("Нельзя поставить лайк повторно");
        }
    }

@Override
    public void deleteLike(Integer id, Integer userId) {
        Film film = getFilmById(id);
        User user = userStorage.getById(userId);
        Set<Integer> likes = film.getLikes();
        if (!likes.remove(user.getId())) {
            log.warn("Лайк пользователя номер - {}не был найден для фильма номер - {}", userId, id);
            throw new NotFoundException("Лайк не найден");
        }
        log.info("Лайк пользователя номер - {} удален для фильма номер - {}", userId, id);
    }

@Override
    public List<Film> getMostPopularFilms(Integer count) {
        if (count < 0) {
            log.warn("Параметр count передан не верно - {}", count);
            throw new IncorrectDataException("Параметр count не может быть меньше 0");
        }
        List<Film> popularFilm = getAllFilms().stream()
                .sorted(Comparator.comparing(Film::getLikesSize).reversed())
                .limit(count)
                .collect(Collectors.toList());
        log.debug("Запрос на вызов популярных фильмов : {}", popularFilm);
        return popularFilm;
    }

}
