package ru.yandex.practicum.filmorate.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.UserStorage;
import ru.yandex.practicum.filmorate.exceptions.EntityDuplicateException;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.mappers.GenreMapper;
import ru.yandex.practicum.filmorate.mappers.MpaMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

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
        } catch (DuplicateKeyException e) {
            throw new EntityDuplicateException("Пользователь с таким логином уже существует в базе");
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

    // 1. Определить фильмы, которые один пролайкал, а другой нет.
    // 2. Найти пользователей с максимальным количеством пересечения по лайкам.
    // 3. Рекомендовать фильмы, которым поставил лайк пользователь с похожими вкусами, а тот, для кого составляется рекомендация, ещё не поставил.
    //  Более подробно подобный алгоритм описан в (https://www.baeldung.com/java-collaborative-filtering-recommendations).
    @Override
    public List<Film> getFilmsRecommendations(int userId) {
        List<Film> filmWithMpaAndGenres = new ArrayList<>();
        List<Film> filmRows = jdbcTemplate.query(
                "select films_model.* from films_likes\n" +
                        "inner join films_model on films_likes.film_id = films_model.film_id\n" +
                        "where user_id in\n" +
                        "--юзеры, у которых есть пресечения с по лайкам со входным юзером\n" +
                        "(select DISTINCT f2.user_id from\n" +
                        "films_likes f1\n" +
                        "inner join films_likes f2 on f1.film_id = f2.film_id\n" +
                        "where f1.user_id = ? and f2.user_id <> ?)\n" +
                        "and films_likes.film_id not IN\n" +
                        "--фильмы, которые уже пролайканы фходным юзером\n" +
                        "(select film_id from films_likes where user_id = ?);", new FilmMapper(), userId, userId, userId);
        for (Film film : filmRows) {
            Mpa mpa = jdbcTemplate.queryForObject("select mpa_dictionary.mpa_id, mpa_dictionary.rating from mpa_dictionary where mpa_id =?", new MpaMapper(), film.getMpa().getId());
            film.setMpa(mpa);
            List<Genre> genres = jdbcTemplate.query("SELECT G2.* FROM films_genres G1 inner JOIN genre_dictionary G2 on G2.genre_id = g1.genre_id WHERE g1.film_id=?;"
                    , new GenreMapper(), film.getId());
            SortedSet<Genre> genresSet = new TreeSet<>(Comparator.comparingInt(Genre::getId));
            genresSet.addAll(genres);
            film.setGenres(genresSet);
            filmWithMpaAndGenres.add(film);
        }
        return filmWithMpaAndGenres;
    }

    private SqlRowSet getUsersSqlRowSet(int id) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("select * from users_model where user_id = ? ", id);
        return userRows;
    }
}
