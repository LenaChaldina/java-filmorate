package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FeedStorage;
import ru.yandex.practicum.filmorate.dao.FilmStorage;
import ru.yandex.practicum.filmorate.dao.LikeStorage;
import ru.yandex.practicum.filmorate.enums.EventTypeEnum;
import ru.yandex.practicum.filmorate.enums.OperationTypeEnum;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

@Service
@Slf4j
public class FilmService {
    private FilmStorage filmStorage;
    private LikeStorage likeStorage;
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
        return filmStorage.getFilms();
    }

    public void putLike(int id, int userId) {
        likeStorage.addLike(id, userId);
        feedStorage.addFeedEvent(userId, id, EventTypeEnum.LIKE, OperationTypeEnum.ADD);
    }

    public void deleteLike(int id, int userId) {
        feedStorage.addFeedEvent(userId, id, EventTypeEnum.LIKE, OperationTypeEnum.REMOVE);
        likeStorage.deleteLike(id, userId);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getPopularFilms(count);
    }

    public Film findFilmById(int id) {
        return filmStorage.findFilmById(id);
    }

    public void deleteFilm(int id) {
        filmStorage.deleteFilm(id);
    }
}

