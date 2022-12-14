package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.UserStorage;
import ru.yandex.practicum.filmorate.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.mappers.UserMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

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

        return user;
    }

    @Override
    public User putUser(User user) {
        jdbcTemplate.update("UPDATE USERS_MODEL SET EMAIL=?, LOGIN=?, NAME=?, BIRTHDAY=? WHERE USER_ID=?",
                user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());

        return user;
    }

    @Override
    public List<User> getUsers() {
        return jdbcTemplate.query("SELECT * FROM USERS_MODEL", new UserMapper());
    }

    @Override
    public User findUserById(int id) {
        List<User> userList = jdbcTemplate.query("SELECT * FROM USERS_MODEL WHERE USER_ID = ?"
                , new UserMapper(), id);
        if (userList.size() == 0) {
            return null;
        }
        return userList.get(0);
    }

    // 1. Определить фильмы, которые один пролайкал, а другой нет.
    // 2. Найти пользователей с максимальным количеством пересечения по лайкам.
    // 3. Рекомендовать фильмы, которым поставил лайк пользователь с похожими вкусами, а тот, для кого составляется рекомендация, ещё не поставил.
    //  Более подробно подобный алгоритм описан в (https://www.baeldung.com/java-collaborative-filtering-recommendations).
    @Override
    public List<Film> getFilmsRecommendations(int userId) {
        List<Film> filmRows = jdbcTemplate.query(
                "select FM.FILM_ID, FM.TITLE, FM.DESCRIPTION, FM.release_date,FM.DURATION, FM.MPA_ID, MD.RATING,\n" +
                        "       TRIM(BOTH ']' from TRIM(BOTH '[' FROM ARRAY_AGG(GD.GENRE_ID))) as GENRE_ID ,\n" +
                        "       TRIM(BOTH ']' from TRIM(BOTH '[' FROM TRIM(BOTH '}' from TRIM(BOTH '{' FROM ARRAY_AGG(GD.GENRE_NAME))))) as GENRE_N, " +
                        "'null' as DIRECTOR_ID, 'null' as DIRECTOR_N \n" +
                        "from films_likes\n" +
                        "                        inner join films_model FM on films_likes.film_id = FM.film_id\n" +
                        "                        inner join MPA_DICTIONARY MD on FM.MPA_ID = MD.MPA_ID\n" +
                        "                        LEFT JOIN films_genres on FM.film_id = films_genres.film_id\n" +
                        "                        LEFT JOIN GENRE_DICTIONARY GD on FILMS_GENRES.GENRE_ID = GD.GENRE_ID\n" +
                        "                        where user_id in\n" +
                        "                        --юзеры, у которых есть пресечения с по лайкам со входным юзером\n" +
                        "                        (select DISTINCT f2.user_id from\n" +
                        "                        films_likes f1\n" +
                        "                        inner join films_likes f2 on f1.film_id = f2.film_id\n" +
                        "                        where f1.user_id = ? and f2.user_id <> ?)\n" +
                        "                        and films_likes.film_id not IN\n" +
                        "                        --фильмы, которые уже пролайканы фходным юзером\n" +
                        "                        (select film_id from films_likes where user_id = ?)\n" +
                        "group by FM.FILM_ID, FM.TITLE, FM.DESCRIPTION, FM.release_date,FM.DURATION, FM.MPA_ID, MD.RATING;", new FilmMapper(), userId, userId, userId);
        return filmRows;
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
