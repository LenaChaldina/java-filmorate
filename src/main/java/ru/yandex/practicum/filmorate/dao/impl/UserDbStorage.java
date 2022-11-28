package ru.yandex.practicum.filmorate.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.UserStorage;
import ru.yandex.practicum.filmorate.exceptions.EntityDuplicateException;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component("UserDbStorage")
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public User addUser(User user) {
        //имя для отображения может быть пустым — в таком случае будет использован логин;
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        try {
            String sqlQuery = "INSERT INTO users_model(email, login, name, birthday) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(sqlQuery,
                    user.getEmail(),
                    user.getLogin(),
                    user.getName(),
                    user.getBirthday());
            log.info("Юзер успешно добавлен");
            return getUser(user);
        } catch(DuplicateKeyException e) { throw new EntityDuplicateException("Пользователь с таким логином уже существует в базе");
        }
    }

    public User getUser(User user) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("select * from users_model where email = ? " +
                        "and login = ? and name = ? and birthday = ?",
                user.getEmail(), user.getLogin(), user.getName(), user.getBirthday());
        if (userRows.next()) {
            User dbUser = new User(userRows.getString("email"),
                    userRows.getString("login"), userRows.getString("name"),
                    userRows.getDate("birthday").toLocalDate());
            dbUser.setId(userRows.getInt("user_id"));
            return dbUser;
        } else {
            return null;
        }
    }

    public User putUser(User user) {
        //имя для отображения может быть пустым — в таком случае будет использован логин;
        //обновляем если такой юзер найден в базе по Id
        if (getUsersSqlRowSet(user.getId()).next()) {
            if (user.getName() == null || user.getName().isBlank()) {
                user.setName(user.getLogin());
            }
            String sqlQuery = "UPDATE users_model SET email = ?, login = ?, name = ?, birthday = ? WHERE users_model.user_id = ?";
            jdbcTemplate.update(sqlQuery,
                    user.getEmail(),
                    user.getLogin(),
                    user.getName(),
                    user.getBirthday(),
                    user.getId());
            log.info("Юзер успешно обновлен");
        } else {
            throw new EntityNotFoundException("Taкого юзера нет в списке");
        }
        return getUser(user);
    }

    public List<User> getUsers() {
        List<User> allUsers = new ArrayList<>();
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("select * from users_model");
        while (userRows.next()) {
            User dbUser = new User(userRows.getString("email"),
                    userRows.getString("login"), userRows.getString("name"),
                    userRows.getDate("birthday").toLocalDate());
            dbUser.setId(userRows.getInt("user_id"));
            allUsers.add(dbUser);
        }
        return allUsers;
    }

    public User findUserById(int id) {
        SqlRowSet rowSet = getUsersSqlRowSet(id);
        if (rowSet.next()) {
                User dbUser = new User(rowSet.getString("email"),
                        rowSet.getString("login"), rowSet.getString("name"),
                        rowSet.getDate("birthday").toLocalDate());
                dbUser.setId(rowSet.getInt("user_id"));
                return dbUser;
        } else {
            throw new EntityNotFoundException("Такого юзера нет");
        }
    }

    private SqlRowSet getUsersSqlRowSet(int id) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("select * from users_model where user_id = ? ", id);
        return userRows;
    }

}
