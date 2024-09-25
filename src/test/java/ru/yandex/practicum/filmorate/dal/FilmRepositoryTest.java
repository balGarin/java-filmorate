package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.hibernate.mapping.List;
import org.hibernate.mapping.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ComponentScan("ru.yandex.practicum.filmorate")
class FilmRepositoryTest {

    private  final FilmRepository filmRepository;


    @Test
    public void shouldReturnCorrectId(){
         Film film = filmRepository.getFilmById(1);
         assertEquals(1,film.getId(),"Id возвращается не верно !");
    }

    @Test
    public void shouldCorrectAddFilm(){
         Film film = new Film();
         film.setName("newFilm");
        MPA mpa = new MPA();
        mpa.setId(1);
        film.setMpa(mpa);
        film.setReleaseDate(LocalDate.now());
        filmRepository.addFilm(film);
        assertEquals(film.getName(),filmRepository.getFilmById(5).getName());
    }

    @Test
    public  void shouldCorrectReturnAllFilms(){

    }
}