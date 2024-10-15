package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
public class Event {
    private Integer id;
    private Integer userId;
    private Integer entityId;
    private Long timestamp;
    private TypeOfEvent eventType;
    private OperationType operation;
}
