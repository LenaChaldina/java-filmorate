package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.DirectorStorage;
import ru.yandex.practicum.filmorate.dao.FeedStorage;
import ru.yandex.practicum.filmorate.dao.FilmStorage;
import ru.yandex.practicum.filmorate.dao.LikeStorage;
import ru.yandex.practicum.filmorate.dao.impl.DirectorDbStorage;
import ru.yandex.practicum.filmorate.enums.EventTypeEnum;
import ru.yandex.practicum.filmorate.enums.OperationTypeEnum;
import ru.yandex.practicum.filmorate.exceptions.RequestError;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final LikeStorage likeStorage;
    private final FeedStorage feedStorage;
    private final DirectorStorage directorStorage;

    @Autowired
    public FilmService(@Qualifier("FilmDbStorage") FilmStorage filmStorage
            , @Qualifier("LikeDbStorage") LikeStorage likeStorage, FeedStorage feedStorage
            , @Qualifier("directorDbStorage") DirectorStorage directorStorage) {
        this.filmStorage = filmStorage;
        this.likeStorage = likeStorage;
        this.feedStorage = feedStorage;
        this.directorStorage = directorStorage;
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
        feedStorage.addFeedEvent(userId, id, EventTypeEnum.LIKE, OperationTypeEnum.ADD);
    }

    public void deleteLike(int id, int userId) {
        likeStorage.checkFilmId(id);
        likeStorage.checkUserId(userId);
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

    public Collection<Film> getDirectorFilmSortedByLike(Integer directorId) {
        if (checkContainsDirectorInList(directorId)) {
            return filmStorage.getDirectorFilmSortedByLike(directorId);
        }
        log.warn("Ошибка. Режиссера с id {} нет в списке", directorId);
        throw new RequestError(HttpStatus.NOT_FOUND, "Режиссер на найден в списке");
    }

    public Collection<Film> getDirectorFilmSortedByYear(Integer directorId) {
        if(checkContainsDirectorInList(directorId)) {
            return filmStorage.getDirectorFilmSortedByYear(directorId);
        }
        log.warn("Ошибка. Режиссера с id {} нет в списке", directorId);
        throw new RequestError(HttpStatus.NOT_FOUND, "Режиссер на найден в списке");
    }


    private boolean checkContainsDirectorInList(Integer id) {
        for (Director director : directorStorage.getAllDirectors()) {
            if (director.getId().equals(id)) return true;
        }
        return false;
    }
}

