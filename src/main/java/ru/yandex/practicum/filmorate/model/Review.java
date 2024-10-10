package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Review {
    private Integer reviewId;
    @NotNull(message = "Тело отзыва не может быть пустым")
    private String content;
    @JsonProperty(value = "isPositive")
    @NotNull
    private Boolean isPositive;
    @NotNull(message = "userId не может быть пустым")
    private Integer userId;
    @NotNull(message = "filmId не может быть пустым")
    private Integer filmId;
    private Integer useful;
}
