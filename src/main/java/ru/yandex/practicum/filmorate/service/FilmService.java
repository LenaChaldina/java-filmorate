package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FeedStorage;
import ru.yandex.practicum.filmorate.dao.FilmStorage;
import ru.yandex.practicum.filmorate.dao.LikeStorage;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.OperationType;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final LikeStorage likeStorage;
    private final FeedStorage feedStorage;

    @Autowired
    public FilmService(@Qualifier("FilmDbStorage") FilmStorage filmStorage, @Qualifier("LikeDbStorage") LikeStorage likeStorage, FeedStorage feedStorage) {
        this.filmStorage = filmStorage;
        this.likeStorage = likeStorage;
        this.feedStorage = feedStorage;
    }

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Film putFilm(Film film) {
        return filmStorage.putFilm(film);
    }

    public List<Film> getFilms() {
        return filmStorage.getListFilms();
    }

    public void putLike(int id, int userId) {
        likeStorage.addLike(id, userId);
        feedStorage.addFeedEvent(Map.of("userId", userId, "entityId", id), EventType.LIKE, OperationType.ADD);
    }

    public void deleteLike(int id, int userId) {
        likeStorage.checkFilmId(id);
        likeStorage.checkUserId(userId);
        feedStorage.addFeedEvent(Map.of("userId", userId, "entityId", id), EventType.LIKE, OperationType.REMOVE);
        likeStorage.deleteLike(id, userId);
    }

    public Film findFilmById(int id) {
        if(filmStorage.checkFilmIdExists(id)) {
            log.warn("Отзыв не создан, фильм с id {} не найден", id);
            throw new EntityNotFoundException("Фильм с таким id не найден");
        }
        return filmStorage.findFilmById(id);
    }

    public void deleteFilm(int id) {
        filmStorage.deleteFilm(id);
    }

    public List<Film> getPopularFilmsWithFilter(int limit, int genreId, int year) {
        return filmStorage.getPopularFilmsWithFilter(limit, genreId, year);
    }
}

