package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FilmStorage;
import ru.yandex.practicum.filmorate.dao.LikeStorage;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

@Service
@Slf4j
public class FilmService {
    private FilmStorage filmStorage;
    private LikeStorage likeStorage;

    @Autowired
    public FilmService(@Qualifier("FilmDbStorage") FilmStorage filmStorage, @Qualifier("LikeDbStorage") LikeStorage likeStorage) {
        this.filmStorage = filmStorage;
        this.likeStorage = likeStorage;
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
    }

    public void deleteLike(int id, int userId) {
        likeStorage.deleteLike(id, userId);
    }

    public Film findFilmById(int id) {
        return filmStorage.findFilmById(id);
    }

    public void deleteFilm(int id) {
        filmStorage.deleteFilm(id);
    }

    public List<Film> getPopularFilmsWithFilter(int limit, int genreId, int year) {
        return filmStorage.getPopularFilmsWithFilter(limit, genreId, year);
    }
}

