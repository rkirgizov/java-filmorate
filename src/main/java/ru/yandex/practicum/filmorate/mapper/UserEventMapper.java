package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.UserEventDto;
import ru.yandex.practicum.filmorate.model.UserEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserEventMapper {

    public static UserEventDto mapToUserEventDto(UserEvent userEvent) {
        UserEventDto dto = new UserEventDto();
        dto.setEventId(userEvent.getEventId());
        dto.setTimestamp(userEvent.getTimestamp());
        dto.setUserId(userEvent.getUserId());
        dto.setEventType(userEvent.getEventType());
        dto.setOperation(userEvent.getOperation());
        dto.setEntityId(userEvent.getEntityId());

        return dto;
    }


}