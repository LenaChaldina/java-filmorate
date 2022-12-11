package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.UserStorage;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.mappers.GenreMapper;
import ru.yandex.practicum.filmorate.mappers.MpaMapper;
import ru.yandex.practicum.filmorate.mappers.UserMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Repository("UserDbStorage")
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User addUser(User user) {
        jdbcTemplate.update("INSERT INTO USERS_MODEL (EMAIL, LOGIN, NAME, BIRTHDAY)" +
                        " VALUES (?, ?, ?, ?) ", user.getEmail(), user.getLogin(), user.getName()
                , user.getBirthday());

        return findUserById(user.getId());
    }

    @Override
    public User putUser(User user) {
        jdbcTemplate.update("UPDATE USERS_MODEL SET EMAIL=?, LOGIN=?, NAME=?, BIRTHDAY=? WHERE USER_ID=?",
                user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());

        return findUserById(user.getId());
    }

    @Override
    public List<User> getUsers() {
        return jdbcTemplate.query("SELECT * FROM USERS_MODEL", new UserMapper());
    }

    @Override
    public User findUserById(int id) {
        return jdbcTemplate.queryForObject("SELECT * FROM USERS_MODEL WHERE USER_ID = ?"
                , new UserMapper(), id);
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

    @Override
    public void deleteUser(int id) {
            removeUserFriends(id);
            removeUserLikes(id);
            removeReviewUser(id);
            removeUserFeed(id);
            String filmSqlQuery = "DELETE FROM users_model WHERE user_id = ?";
            jdbcTemplate.update(filmSqlQuery, id);
    }

    private void removeUserFriends(int id) {
        String sqlQuery = "DELETE FROM users_friends WHERE user_id = ? or USER_FRIEND_ID = ?";
        jdbcTemplate.update(sqlQuery, id, id);
    }

    private void removeUserLikes(int id) {
        String sqlQuery = "DELETE FROM films_likes WHERE user_id = ?";
        jdbcTemplate.update(sqlQuery, id);
    }

    private void removeUserFeed(int id) {
        String sqlQuery = "DELETE FROM FEED_MODEL WHERE user_id = ?";
        jdbcTemplate.update(sqlQuery, id);
    }

    private void removeReviewUser(int id) {
        jdbcTemplate.update("DELETE FROM REVIEW_LIKES WHERE user_id = ?", id);
        jdbcTemplate.update("DELETE FROM REVIEWS WHERE user_id = ?", id);
    }

    public SqlRowSet getUsersSqlRowSet(int id) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("select * from users_model where user_id = ? ", id);
        return userRows;
    }
}
