package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;

import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.model.User;


import java.time.LocalDate;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ComponentScan("ru.yandex.practicum.filmorate")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class FilmRepositoryTest {

    final FilmRepository filmRepository;
    final UserRepository userRepository;


    @Test
    public void shouldCorrectAddFilmAndFindByID() {

        Film film = new Film();
        film.setName("newFilm");
        MPA mpa = new MPA();
        mpa.setId(1);
        film.setMpa(mpa);
        film.setReleaseDate(LocalDate.now());
        filmRepository.addFilm(film);
        assertEquals(film.getName(), filmRepository.getFilmById(1).getName(), "Имя не совпадает ");
        assertEquals(1, filmRepository.getFilmById(1).getId(), "Id не совпадает");
    }

    @Test
    public void shouldCorrectReturnAllFilms() {
        Film film1 = new Film();
        film1.setName("newFilm");
        film1.setDuration(122);
        MPA mpa1 = new MPA();
        mpa1.setName("G");
        mpa1.setId(1);
        film1.setMpa(mpa1);
        film1.setReleaseDate(LocalDate.now());
        filmRepository.addFilm(film1);
        Film film2 = new Film();
        film2.setName("newFilm");
        film2.setDuration(122);
        MPA mpa2 = new MPA();
        mpa2.setName("G");
        mpa2.setId(1);
        film2.setMpa(mpa2);
        film2.setReleaseDate(LocalDate.now());
        filmRepository.addFilm(film2);
        List<Film> filmList = List.of(film1, film2);
        List<Film> filmsFromDB = filmRepository.getAllFilms();
        assertEquals(filmList.get(1).getName(), filmsFromDB.get(1).getName(), "Фильмы не совпадают ");
        assertEquals(2, filmsFromDB.size(), "Размер не корректный");


    }


    @Test
    public void shouldCorrectUpdateFilm() {
        Film film = new Film();
        film.setName("newFilm");
        film.setDuration(122);
        MPA mpa1 = new MPA();
        mpa1.setName("G");
        mpa1.setId(1);
        film.setMpa(mpa1);
        film.setReleaseDate(LocalDate.now());
        filmRepository.addFilm(film);
        film.setId(1);
        film.setName("updatedName");
        film.setDescription("updatedDescription");
        filmRepository.updateFilm(film);
        assertEquals(film, filmRepository.getFilmById(1));
    }

    @Test
    public void shouldAddLikeAndDeleteLike() {
        User user = new User();
        user.setLogin("login");
        user.setEmail("garin@gmail.com");
        user.setBirthday(LocalDate.now());
        userRepository.addUser(user);
        Film film = new Film();
        film.setName("newFilm");
        film.setDuration(122);
        MPA mpa1 = new MPA();
        mpa1.setName("G");
        mpa1.setId(1);
        film.setMpa(mpa1);
        film.setReleaseDate(LocalDate.now());
        filmRepository.addFilm(film);
        filmRepository.addLike(1, 1);
        assertEquals(1, filmRepository.getLikes(1).size(), "Лайк не появился");
        assertEquals(1, filmRepository.getLikes(1).get(0), "Id юзера не совпадает");
        filmRepository.deleteLike(1, 1);
        assertEquals(0, filmRepository.getLikes(1).size(), "Лайк не удалился");
    }

    @Test
    public void shouldCorrectReturnListOfPopularFilms() {
        User user1 = new User();
        user1.setLogin("login1");
        user1.setEmail("garin@gmail.com");
        user1.setBirthday(LocalDate.now());
        userRepository.addUser(user1);
        User user2 = new User();
        user2.setLogin("login2");
        user2.setEmail("garin@gmail.com");
        user2.setBirthday(LocalDate.now());
        userRepository.addUser(user2);
        User user3 = new User();
        user3.setLogin("login1");
        user3.setEmail("garin@gmail.com");
        user3.setBirthday(LocalDate.now());
        userRepository.addUser(user3);
        Film film1 = new Film();
        film1.setName("newFilm1");
        film1.setDuration(122);
        film1.setLikes(new HashSet<>(List.of(1, 2)));
        MPA mpa1 = new MPA();
        mpa1.setName("G");
        mpa1.setId(1);
        film1.setMpa(mpa1);
        film1.setReleaseDate(LocalDate.now());
        filmRepository.addFilm(film1);
        Film film2 = new Film();
        film2.setName("newFilm2");
        film2.setDuration(122);
        film2.setLikes(new HashSet<>(List.of(1)));
        MPA mpa2 = new MPA();
        mpa2.setName("G");
        mpa2.setId(1);
        film2.setMpa(mpa2);
        film2.setReleaseDate(LocalDate.now());
        filmRepository.addFilm(film2);
        Film film3 = new Film();
        film3.setName("newFilm3");
        film3.setDuration(122);
        film3.setLikes(new HashSet<>(List.of(1, 2, 3)));
        MPA mpa3 = new MPA();
        mpa3.setName("G");
        mpa3.setId(1);
        film3.setMpa(mpa3);
        film3.setReleaseDate(LocalDate.now());
        filmRepository.addFilm(film3);
        filmRepository.addLike(3, 1);
        filmRepository.addLike(3, 2);
        filmRepository.addLike(3, 3);
        filmRepository.addLike(1, 1);
        filmRepository.addLike(1, 2);
        filmRepository.addLike(2, 1);
        LinkedList<Film> films = new LinkedList<>();
        films.add(film3);
        films.add(film1);
        films.add(film2);
        List<Film> filmsFromDB = filmRepository.getMostPopularFilms(3);
        assertEquals(films.size(), filmsFromDB.size(), "Count работает не верно");
        assertEquals(films.get(0).getName(), filmsFromDB.get(0).getName(), "Не верно работает сортировка");
    }

}