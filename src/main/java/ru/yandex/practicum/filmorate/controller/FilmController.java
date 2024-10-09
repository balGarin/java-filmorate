package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;


import java.util.*;

@RestController
@RequestMapping("/films")
@Validated
@AllArgsConstructor
public class FilmController {

    private final FilmService filmService;


    @PostMapping
    public Film addFilm(@RequestBody @Valid Film newFilm) {
        return filmService.addFilm(newFilm);
    }


    @PutMapping
    public Film updateFilm(@RequestBody @Valid Film newFilm) {
        return filmService.updateFilm(newFilm);
    }


    @GetMapping
    public List<Film> getAllFilms() {
        return filmService.getAllFilms();
    }

    @GetMapping("/{id}")
    public Film getFilmByID(@PathVariable Integer id) {
        return filmService.getFilmById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Integer id, @PathVariable Integer userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("{id}/like/{userId}")
    public void deleteLike(@PathVariable Integer id, @PathVariable Integer userId) {
        filmService.deleteLike(id, userId);
    }

    /**
     * Вывод самых популярных фильмов по жанру и годам.
     *
     * @param count   количество топ фильмов, 10 по умолчанию,
     * @param genreId айди жанра, для фильтрации по жанру,
     * @param year    год выходы фильма, для фильтрации по году,
     * @return Возвращает список самых популярных фильмов указанного жанра за нужный год.
     */
    @GetMapping("/popular")
    public List<Film> getMostPopularFilms(@RequestParam(required = false, defaultValue = "10") Integer count,
                                          @RequestParam Optional<Integer> genreId,
                                          @RequestParam Optional<Integer> year) {
        return filmService.getMostPopular(count, genreId, year);
    }

}



