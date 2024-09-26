package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ComponentScan("ru.yandex.practicum.filmorate")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class GenreRepositoryTest {

    private final GenreRepository genreRepository;

    @Test
    public void shouldCorrectFindByIdAndAll() {
        Genre genreComedy = new Genre();
        genreComedy.setId(1);
        genreComedy.setName("Комедия");
        assertEquals(genreComedy, genreRepository.getGenreById(1));
        Genre genreDrama = new Genre();
        genreDrama.setId(2);
        genreDrama.setName("Драма");
        Genre genreCartoon = new Genre();
        genreCartoon.setId(3);
        genreCartoon.setName("Мультфильм");
        Genre genreThriller = new Genre();
        genreThriller.setId(4);
        genreThriller.setName("Триллер");
        Genre genreDocumentary = new Genre();
        genreDocumentary.setId(5);
        genreDocumentary.setName("Документальный");
        Genre genreAction = new Genre();
        genreAction.setId(6);
        genreAction.setName("Боевик");
        List<Genre> genres = List.of(genreComedy, genreDrama, genreCartoon, genreThriller, genreDocumentary,
                genreAction);
        assertEquals(genres.size(), genreRepository.getAllGenre().size(), "Количество жанров не совпадает");
        assertEquals(genres, genreRepository.getAllGenre(), "Жанры отображаются не корректно");

    }

}