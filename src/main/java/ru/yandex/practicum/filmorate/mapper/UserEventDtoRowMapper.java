package ru.yandex.practicum.filmorate.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.UserEventDto;
import ru.yandex.practicum.filmorate.enumeration.EventOperation;
import ru.yandex.practicum.filmorate.enumeration.EventType;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class UserEventDtoRowMapper implements RowMapper<UserEventDto> {
    @Override
    public UserEventDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        UserEventDto dto = new UserEventDto();
        dto.setEventId(rs.getInt("id"));
        dto.setTimestamp(rs.getLong("timestamp"));
        dto.setUserId(rs.getInt("user_id"));
        dto.setEventType(EventType.valueOf(rs.getString("event_type")));
        dto.setOperation(EventOperation.valueOf(rs.getString("operation")));
        dto.setEntityId(rs.getInt("entity_id"));
        return dto;
    }
}