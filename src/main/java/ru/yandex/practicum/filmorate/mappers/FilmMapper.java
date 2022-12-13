package ru.yandex.practicum.filmorate.mappers;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class FilmMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet rowSet, int rowNum) throws SQLException {
        //Добавила жанры в маппеер, чтобы получать готовый фильм одним запросом из базы
        String ids = rowSet.getString("GENRE_ID");
        String directorId = rowSet.getString("DIRECTOR_ID");

        Film film = new Film(
                rowSet.getString("title"),
                rowSet.getString("description"),
                rowSet.getDate("release_date").toLocalDate(),
                rowSet.getLong("duration"),
                new Mpa(rowSet.getInt("mpa_id"), rowSet.getString("RATING")));
        film.setId(rowSet.getInt("film_id"));
        film.setGenres(getGenresForFilm(rowSet, ids));
        film.setDirectors(getDirectorsForFilm(rowSet, directorId));
        return film;
    }

    private SortedSet<Genre> getGenresForFilm(ResultSet rowSet, String ids) throws SQLException {
        SortedSet<Genre> genres = new TreeSet<>(Comparator.comparingInt(Genre::getId));
        if (!ids.contains("null")) {
            int[] genreIds = Arrays.stream(ids.split(", ")).mapToInt(Integer::parseInt).toArray();
            String[] genreNames = rowSet.getString("GENRE_N").split(", ");

            for (int i = 0; i < genreIds.length; i++) {
                genres.add(new Genre(genreIds[i], genreNames[i]));
            }
        }
        return genres;
    }

    private List<Director> getDirectorsForFilm(ResultSet rowSet, String directorId) throws SQLException {
        List<Director> directors = new ArrayList<>();
        if (!directorId.contains("null")) {
            int[] directorIds = Arrays.stream(directorId.split(", ")).mapToInt(Integer::parseInt).toArray();
            String[] genreNames = rowSet.getString("DIRECTOR_N").split(", ");

            for (int i = 0; i < directorIds.length; i++) {
                directors.add(new Director(directorIds[i], genreNames[i]));
            }
        }
        return directors;
    }
}
