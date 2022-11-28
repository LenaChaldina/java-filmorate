package ru.yandex.practicum.filmorate.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.LikeStorage;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;

@Slf4j
@Component("LikeDbStorage")

public class LikeDbStorage implements LikeStorage {
    private final JdbcTemplate jdbcTemplate;

    public LikeDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void addLike(int filmId, int userId) {
        //если существует фильм и юзер
        checkFilmId(filmId);
        checkUserId(userId);
        String sqlQuery = "INSERT INTO films_likes(film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sqlQuery, filmId, userId);
        log.info("Пользователь с id " + userId + " поставил лайк фильму с id " + filmId + ".");


    }

    @Override
    public void deleteLike(int filmId, int userId) {
        checkFilmId(filmId);
        checkUserId(userId);
        String sqlQuery = "DELETE FROM films_likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sqlQuery, filmId, userId);
        log.info("Пользователь с id " + userId + " убрал лайк с фильма с id " + filmId + ".");
    }

    private void checkFilmId(int filmId) {
        String sqlQuery = "SELECT * FROM films_model WHERE film_id = ?";
        SqlRowSet filmRow = jdbcTemplate.queryForRowSet(sqlQuery, filmId);
        if (!filmRow.next()) {
            throw new EntityNotFoundException("Фильм с id " + filmId + " не найден.");
        }
    }

    private void checkUserId(int userId) {
        String sqlQuery = "SELECT * FROM users_model WHERE user_id = ?";
        SqlRowSet userRow = jdbcTemplate.queryForRowSet(sqlQuery, userId);
        if (!userRow.next()) {
            throw new EntityNotFoundException("Пользователь с id " + userId + " не найден.");
        }
    }
}
