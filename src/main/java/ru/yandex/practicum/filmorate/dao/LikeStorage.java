package ru.yandex.practicum.filmorate.dao;

import org.springframework.jdbc.support.rowset.SqlRowSet;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;

public interface LikeStorage {
    void addLike(int film_id, int user_id);
    void deleteLike(int film_id, int user_id);
    void checkFilmId(int filmId);
    void checkUserId(int userId);
}
