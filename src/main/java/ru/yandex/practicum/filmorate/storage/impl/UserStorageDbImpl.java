package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.UserFriendDto;
import ru.yandex.practicum.filmorate.mapper.UserFriendDtoRowMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.BaseStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component("UserStorageDbImpl")
public class UserStorageDbImpl extends BaseStorage<User> implements UserStorage {
    private static final String FIND_BY_ID_QUERY = " SELECT * FROM _user WHERE id = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM _user";
    private static final String INSERT_QUERY = "INSERT INTO _user (login,email,name,birthday_dt) VALUES (?,?,?,?)";
    private static final String UPDATE_QUERY = "UPDATE _user SET login = ?, email = ?, name = ?, birthday_dt = ? WHERE id = ?";
    private static final String DELETE_QUERY = "DELETE FROM _user WHERE id = ?";
    private static final String ADD_USER_FRIEND_QUERY = "INSERT INTO _user_friend (user_id, friend_id, status_id) SELECT ?, ?, ?";
    private static final String REMOVE_USER_FRIEND_QUERY = "DELETE FROM _user_friend WHERE (user_id = ? AND friend_id = ?)";
    private static final String FIND_FRIENDS_BY_USERID_QUERY = "SELECT u.*, fs.name AS friendship_status FROM _user u " +
            "JOIN _user_friend uf ON u.id = uf.friend_id " +
            "JOIN _friend_status fs ON uf.status_id = fs.id " +
            "WHERE uf.user_id  = ?";

    public UserStorageDbImpl(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Optional<User> findUserById(int userId) {
        return findOne(FIND_BY_ID_QUERY, userId);
    }

    @Override
    public List<User> findAllUsers() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public User createUser(User user) {
        int id = insert(
                INSERT_QUERY,
                user.getLogin(),
                user.getEmail(),
                user.getName(),
                user.getBirthday()
        );
        user.setId(id);
        log.debug("Пользователь с id = {} - добавлен", user.getId());
        return user;
    }

    @Override
    public User updateUser(User user) {
        update(
                UPDATE_QUERY,
                user.getLogin(),
                user.getEmail(),
                user.getName(),
                Date.valueOf(user.getBirthday()).toString(),
                user.getId());
        return user;
    }

    @Override
    public void deleteUser(int userId) {
        delete(DELETE_QUERY, userId);
    }

    @Override
    public void addFriendRequest(int userId, int friendId) {
        insert(
                ADD_USER_FRIEND_QUERY,
                userId,
                friendId,
                1);
    }

    @Override
    public void deleteFriend(int userId, int friendId) {
        delete(REMOVE_USER_FRIEND_QUERY, userId, friendId);
    }

    @Override
    public List<UserFriendDto> findFriendsByUserId(int userId) {
        return jdbc.query(FIND_FRIENDS_BY_USERID_QUERY, new UserFriendDtoRowMapper(), userId);
    }

}












