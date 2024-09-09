package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FilmStorageTests {

    @Test
    public void shouldNotEmptyName() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Film nullName = new Film();
        nullName.setName(null);
        Set<ConstraintViolation<Film>> violations = validator.validate(nullName);
        assertFalse(violations.isEmpty(), "Валидация пропустила Null");
        Film notNullName = new Film();
        notNullName.setName("Dmitry");
        violations = validator.validate(notNullName);
        assertTrue(violations.isEmpty(), "Валидация работает не корректно!");
    }

    @Test
    public void shouldMaxLengthOfDescriptionIs200() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Film filmDescriptionOver200 = new Film();
        filmDescriptionOver200.setName("name");
        filmDescriptionOver200.setDescription("1".repeat(250));
        Set<ConstraintViolation<Film>> violations = validator.validate(filmDescriptionOver200);
        assertFalse(violations.isEmpty(), "Валидация пропустила описание свыше 200 символов");
        Film filmDescriptionExactly200 = new Film();
        filmDescriptionExactly200.setName("name");
        filmDescriptionExactly200.setDescription("1".repeat(200));
        violations = validator.validate(filmDescriptionExactly200);
        assertTrue(violations.isEmpty(), "Валидация граничного значения 200 работает не верно");
        Film filmDescriptionLess200 = new Film();
        filmDescriptionLess200.setName("name");
        filmDescriptionLess200.setDescription("description");
        violations = validator.validate(filmDescriptionLess200);
        assertTrue(violations.isEmpty(), "Валидация работает не верно,при доступном значение");
    }

    @Test
    public void shouldNotReleaseDateEarlier1895() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Film filmReleaseEarlier1896 = new Film();
        filmReleaseEarlier1896.setName("name");
        filmReleaseEarlier1896.setReleaseDate(LocalDate.of(1885, 12, 12));
        Set<ConstraintViolation<Film>> violations = validator.validate(filmReleaseEarlier1896);
        assertFalse(violations.isEmpty(), "Валидация не корректна при значение раньше 1895");
        Film filmReleaseExactly1895 = new Film();
        filmReleaseExactly1895.setName("name");
        filmReleaseExactly1895.setReleaseDate(LocalDate.of(1895, 12, 28));
        violations = validator.validate(filmReleaseExactly1895);
        assertTrue(violations.isEmpty(), "Валидация не корректна при граничном значение 1895,12,28");
        Film filmReleaseLater1896 = new Film();
        filmReleaseLater1896.setName("name");
        filmReleaseLater1896.setReleaseDate(LocalDate.of(1992, 12, 21));
        violations = validator.validate(filmReleaseLater1896);
        assertTrue(violations.isEmpty(), "Валидация не корректна при доступном значение");
    }

    @Test
    public void shouldFilmDurationIsPositive() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Film filmPositive = new Film();
        filmPositive.setName("name");
        filmPositive.setDuration(120);
        Set<ConstraintViolation<Film>> violations = validator.validate(filmPositive);
        assertTrue(violations.isEmpty(), "Валидация не корректна при доступном значение");
        Film filmNegative = new Film();
        filmNegative.setName("name");
        filmNegative.setDuration(-1);
        violations = validator.validate(filmNegative);
        assertFalse(violations.isEmpty(), "Валидация не корректна при отрицательной длительности");

    }


}
