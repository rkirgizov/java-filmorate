package ru.yandex.practicum.filmorate.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.filmorate.enumeration.EventOperation;
import ru.yandex.practicum.filmorate.enumeration.EventType;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserEvent {
    Integer eventId;
    Long timestamp;
    Integer userId;
    EventType eventType;
    EventOperation operation;
    Integer entityId;
}