package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@RestController
@RequestMapping("/films")
@Validated()
public class FilmController {
    Map<Integer, Film> films = new HashMap<>();
    final Logger log = LoggerFactory.getLogger(FilmController.class);

    @PostMapping
    public Film addFilm(@RequestBody @Valid Film newFilm) {
        log.debug("Валидация успешно пройдена!");
        newFilm.setId(generateId());
        films.put(newFilm.getId(), newFilm);
        log.info("Добавлен новый фильм {}", newFilm.toString());
        return newFilm;
    }


    @PutMapping
    public Film updateFilm(@RequestBody @Valid Film newFilm) {
        log.debug("Валидация успешно пройдена!");
        log.info("Новый фильм для обновления {}", newFilm.toString());
        if (newFilm.getId() == null || !films.containsKey(newFilm.getId())) {
            log.warn("Ошибка валидации : указан некорректный id");
            throw new ValidationException("Ошибка валидации : указан некорректный id");
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


    @GetMapping
    public List<Film> getFilms() {
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



