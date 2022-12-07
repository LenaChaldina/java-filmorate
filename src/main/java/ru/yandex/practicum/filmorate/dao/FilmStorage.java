package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    Film addFilm(Film film);

    Film putFilm(Film film);

    List<Film> getListFilms();

    Film findFilmById(int id);
    void deleteFilm(int id);

    List<Film> getPopularFilmsWithFilter(int limit, int genreId, int year);
}
