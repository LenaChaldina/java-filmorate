package ru.yandex.practicum.filmorate.dao;

import org.springframework.jdbc.support.rowset.SqlRowSet;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {
    Film addFilm(Film film);

    Film putFilm(Film film);

    List<Film> getListFilms();

    Film findFilmById(int id);

    void deleteFilm(int id);

    List<Film> getPopularFilmsWithFilter(int limit, int genreId, int year);

    Collection<Film> getDirectorFilmSortedByLike(Integer directorId);

    Collection<Film> getDirectorFilmSortedByYear(Integer directorId);

    boolean checkFilmIdExists(int id);

    List<Film> searchFilm(String query, List<String> searchBy);

    List<Film> getCommonFilms(Integer userId, Integer friendId);

    SqlRowSet getSqlRowSetByFilmId(Integer id);

    SqlRowSet getFilmsSqlRowSet(int id);
}
