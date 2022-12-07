package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Film;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public interface FilmStorage {
    Film addFilm(Film film);

    Film putFilm(Film film);

    List<Film> getListFilms();

    Film findFilmById(int id);

    List<Film> getPopularFilms(int count);
    void deleteFilm(int id);

    Collection<Film> getDirectorFilmSortedByLike(Integer directorId);

    Collection<Film> getDirectorFilmSortedByYear(Integer directorId);
}
