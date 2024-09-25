package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.IncorrectDataException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilmServiceTest {


    @Test
    public void shouldCorrectAddLike() {
       InMemoryUserStorage userStorage = new InMemoryUserStorage();
        InMemoryFilmStorage filmStorage = new InMemoryFilmStorage(userStorage);
        FilmService service = new FilmService(filmStorage);
        User user = new User();
        user.setLogin("login");
        user.setEmail("yandex@gmai.ru");
        userStorage.addUser(user);
        Film film = new Film();
        film.setName("name");
        filmStorage.addFilm(film);
        service.addLike(filmStorage.getFilmById(1).getId(), userStorage.getById(1).getId());
        assertEquals(1, film.getLikesSize(), "Количество лайков не корректно");
        boolean exceptionThrown = false;
        try {
            service.addLike(film.getId(), user.getId());
        } catch (IncorrectDataException e) {
            exceptionThrown = true;
            assertEquals("Нельзя поставить лайк повторно", e.getMessage(), "Не верное сообщение");
        }
        assertTrue(exceptionThrown, "Исключение не было выброшено");
    }

    @Test
    public void shouldCorrectDeleteLike() {
        InMemoryUserStorage userStorage = new InMemoryUserStorage();
        InMemoryFilmStorage filmStorage = new InMemoryFilmStorage(userStorage);
        FilmService service = new FilmService(filmStorage);
        User user = new User();
        user.setLogin("login");
        user.setEmail("yandex@gmai.ru");
        userStorage.addUser(user);
        Film film = new Film();
        film.setName("name");
        filmStorage.addFilm(film);
        service.addLike(film.getId(), user.getId());
        assertEquals(1, film.getLikesSize(), "Количество лайков не корректно");
        service.deleteLike(film.getId(), user.getId());
        assertEquals(0, film.getLikesSize(), "Количество лайков не корректно");
    }

    @Test
    public void shouldCorrectMappingPopularFilms() {
        InMemoryUserStorage userStorage = new InMemoryUserStorage();
        InMemoryFilmStorage filmStorage = new InMemoryFilmStorage(userStorage);
        FilmService service = new FilmService(filmStorage);
        User user1 = new User();
        user1.setLogin("login");
        user1.setEmail("yandex@gmai.ru");
        userStorage.addUser(user1);
        User user2 = new User();
        user2.setLogin("login");
        user2.setEmail("yandex@gmai.ru");
        userStorage.addUser(user2);
        User user3 = new User();
        user3.setLogin("login");
        user3.setEmail("yandex@gmai.ru");
        userStorage.addUser(user3);
        Film film1 = new Film();
        film1.setName("name");
        filmStorage.addFilm(film1);
        Film film2 = new Film();
        film2.setName("name2");
        filmStorage.addFilm(film2);
        Film film3 = new Film();
        film3.setName("name3");
        filmStorage.addFilm(film3);
        Film film4 = new Film();
        film4.setName("name4");
        filmStorage.addFilm(film4);
        service.addLike(film1.getId(), user1.getId());
        service.addLike(film1.getId(), user2.getId());
        service.addLike(film2.getId(), user1.getId());
        service.addLike(film3.getId(), user1.getId());
        service.addLike(film3.getId(), user2.getId());
        service.addLike(film3.getId(), user3.getId());
        List<Film> listOfFilms = List.of(film3, film1, film2);
        List<Film> popularFilms = service.getMostPopularFilms(3);
        assertEquals(listOfFilms.size(), popularFilms.size(), "Значение 'count' работает не верно");
        assertEquals(listOfFilms, popularFilms, "Рейтинг по лайкам работает не корректно");
    }


}