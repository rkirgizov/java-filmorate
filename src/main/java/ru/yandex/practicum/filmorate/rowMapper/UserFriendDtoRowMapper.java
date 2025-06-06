package ru.yandex.practicum.filmorate.rowMapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.UserFriendDto;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class UserFriendDtoRowMapper implements RowMapper<UserFriendDto> {
    @Override
    public UserFriendDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        UserFriendDto dto = new UserFriendDto();
        dto.setId(rs.getInt("id"));
        dto.setLogin(rs.getString("login"));
        dto.setEmail(rs.getString("email"));
        dto.setName(rs.getString("name"));
        dto.setBirthday(rs.getDate("birthday_dt").toLocalDate());
        dto.setFriendshipStatus(rs.getString("friendship_status"));
        return dto;
    }
}