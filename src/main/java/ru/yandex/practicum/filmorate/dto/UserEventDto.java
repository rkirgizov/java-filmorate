package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.yandex.practicum.filmorate.enumeration.EventOperation;
import ru.yandex.practicum.filmorate.enumeration.EventType;

@Data
public class UserEventDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer eventId;
    private Long timestamp;
    private Integer userId;
    private EventType eventType;
    private EventOperation operation;
    private Integer entityId;
}