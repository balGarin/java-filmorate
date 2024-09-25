package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Value;
import org.hibernate.annotations.SQLInserts;

@Data
public class MPA {
      private Integer id;
      private String name;
}
