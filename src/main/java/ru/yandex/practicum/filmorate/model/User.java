package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.ConsistentLoginParameters;

import java.time.LocalDate;

@Data
public class User {
    private Integer id;

    private String name;
    @NotNull(message = "login не может быть пустым")
    @ConsistentLoginParameters
    private String login;
    @NotNull(message = "email не может быть пустым")
    @Email(message = "Не корректный email!")
    private String email;
    @Past(message = "Дата рождения не может быть в будущем!")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;
}
