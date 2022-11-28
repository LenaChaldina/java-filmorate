package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    Film addFilm(Film film);

    Film putFilm(Film film);

    List<Film> getFilms();

    Film findFilmById(int id);

    List<Film> getPopularFilms(int count);
    void deleteFilm(int id);
}
