package ru.yandex.practicum.filmorate.dao;

public interface LikeStorage {
    public void addLike(int film_id, int user_id);
    public void deleteLike(int film_id, int user_id);
}
