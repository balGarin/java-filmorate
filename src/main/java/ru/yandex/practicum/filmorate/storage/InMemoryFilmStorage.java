package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.IncorrectDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private Map<Integer, Film> films = new HashMap<>();

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
}
