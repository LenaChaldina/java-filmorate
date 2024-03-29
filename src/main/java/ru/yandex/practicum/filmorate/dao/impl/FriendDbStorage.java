package ru.yandex.practicum.filmorate.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.FriendStorage;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.mappers.UserMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository("FriendDbStorage")
public class FriendDbStorage implements FriendStorage {
    private final JdbcTemplate jdbcTemplate;

    public FriendDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addFriend(int userId, int friendId) {
        userExists(userId);
        userExists(friendId);
        String sqlQuery = "INSERT INTO users_friends(user_id, user_friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sqlQuery,
                userId,
                friendId);
    }

    public void deleteFriend(int userId, int friendId) {
        userExists(userId);
        userExists(friendId);
        String sqlQuery = "DELETE FROM users_friends WHERE USER_ID = ? AND USER_FRIEND_ID = ?";
        jdbcTemplate.update(sqlQuery, userId, friendId);
    }

    public List<User> getFriends(int id) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("select m.* from users_friends f inner join users_model m on f.user_friend_id = m.user_id where f.user_id = ?", id);
        List<User> usersFromDb = getUsersFromDb(userRows);
        return usersFromDb;

    }

    public Boolean getFriendForUser(Integer userId, Integer friendId) {
        List<User> userList = jdbcTemplate.
                query("select m.* from users_friends f inner join users_model m on f.user_friend_id = m.user_id " +
                                "where f.user_id = ? and f.friends_id = ?"
                        , new UserMapper(), userId, friendId);
        if (userList.size() == 0) {
            return true;
        }
        return false;
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("select m.* from users_friends u inner join users_friends f on u.user_friend_id = f.user_friend_id inner join users_model m on m.user_id = u.user_friend_id where u.user_id = ? and f.user_id = ?", userId, otherId);
        return getUsersFromDb(userRows);
    }

    private List<User> getUsersFromDb(SqlRowSet userRows) {
        List<User> usersFromDb = new ArrayList<>();
        while (userRows.next()) {
            User dbUser = new User(userRows.getString("email"),
                    userRows.getString("login"), userRows.getString("name"),
                    userRows.getDate("birthday").toLocalDate());
            dbUser.setId(userRows.getInt("user_id"));
            usersFromDb.add(dbUser);
        }
        return usersFromDb;
    }

    private void userExists(int id) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("select * from users_model where user_id = ?", id);
        if (userRows.next()) {
            log.info("User" + id + "найден");
        } else {
            log.info("User" + id + "отсутствует");
            throw new EntityNotFoundException("Пользователь не найден");
        }
    }
}
