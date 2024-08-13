package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.ConsistentDateParameters;

import java.time.LocalDate;


@Data
public class Film {
    private Integer id;
    @NotEmpty(message = "Название не может быть пустым !")
    private String name;
    @Size(max = 200, message = "Описание не может превышать 200 символов !")
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @ConsistentDateParameters
    private LocalDate releaseDate;
    @Positive(message = "Длительность должна быть положительным числом")
    private Long duration;
}
