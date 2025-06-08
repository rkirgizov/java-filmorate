package ru.yandex.practicum.filmorate.rowMapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.enumeration.EventOperation;
import ru.yandex.practicum.filmorate.enumeration.EventType;
import ru.yandex.practicum.filmorate.model.UserEvent;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class UserEventRowMapper implements RowMapper<UserEvent> {
    @Override
    public UserEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
        UserEvent userEvent = new UserEvent();
        userEvent.setEventId(rs.getInt("id"));
        userEvent.setTimestamp(rs.getLong("timestamp"));
        userEvent.setUserId(rs.getInt("user_id"));
        userEvent.setEventType(EventType.valueOf(rs.getString("event_type")));
        userEvent.setOperation(EventOperation.valueOf(rs.getString("operation")));
        userEvent.setEntityId(rs.getInt("entity_id"));
        return userEvent;
    }
}