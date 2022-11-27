package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    public Film addFilm(Film film);

    public Film putFilm(Film film);

    public List<Film> getFilms();

    public Film findFilmById(int id);

    List<Film> getPopularFilms(int count);
    public void deleteFilm(int id);
}
