package ru.yandex.practicum.filmorate.storage;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.User;

import static org.junit.jupiter.api.Assertions.*;


import java.time.LocalDate;
import java.util.Set;

@SpringBootTest
public class UserStorageTest {
    @Test
    public void shouldCorrectValidationEmail() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        User userWithEmptyEmail = new User();
        userWithEmptyEmail.setLogin("login");
        userWithEmptyEmail.setEmail(null);
        Set<ConstraintViolation<User>> violations = validator.validate(userWithEmptyEmail);
        assertFalse(violations.isEmpty(), "Не корректная валидация при значение null");
        User userWithIncorrectEmail = new User();
        userWithIncorrectEmail.setLogin("login");
        userWithIncorrectEmail.setEmail("practicum_yandex.ru");
        violations = validator.validate(userWithIncorrectEmail);
        assertFalse(violations.isEmpty(), "Валидация не корректна при не корректном email");
        User userWithCorrectEmail = new User();
        userWithCorrectEmail.setLogin("login");
        userWithCorrectEmail.setEmail("practicum@mail.ru");
        violations = validator.validate(userWithCorrectEmail);
        assertTrue(violations.isEmpty(), "Валидация не корректна при доступном значение");
    }

    @Test
    public void shouldCorrectValidationLogin() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        User userNullLogin = new User();
        userNullLogin.setLogin(null);
        userNullLogin.setEmail("practicum@mail.ru");
        Set<ConstraintViolation<User>> violations = validator.validate(userNullLogin);
        assertFalse(violations.isEmpty(), "Не корректная валидация при значение null");
        User userWhitespaceLogin = new User();
        userWhitespaceLogin.setLogin("Lo gin");
        userWhitespaceLogin.setEmail("practicum@mail.ru");
        violations = validator.validate(userWhitespaceLogin);
        assertFalse(violations.isEmpty(), "Валидация не корректна при наличие пробела");
    }

    @Test
    public void shouldNotBirthdayInTheFuture() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        User userWithIncorrectBirthday = new User();
        userWithIncorrectBirthday.setLogin("login");
        userWithIncorrectBirthday.setEmail("practicum@mail.ru");
        userWithIncorrectBirthday.setBirthday(LocalDate.of(2025, 12, 12));
        Set<ConstraintViolation<User>> violations = validator.validate(userWithIncorrectBirthday);
        assertFalse(violations.isEmpty(), "Валидация не корректна при значение в будущем");
        User userWithCorrectBirthday = new User();
        userWithCorrectBirthday.setLogin("login");
        userWithCorrectBirthday.setEmail("practicum@mail.ru");
        userWithCorrectBirthday.setBirthday(LocalDate.of(1992, 12, 12));
        violations = validator.validate(userWithCorrectBirthday);
        System.out.println(violations);
        assertTrue(violations.isEmpty(), "Валидация не корректна при допустимом значение ");
    }


}
