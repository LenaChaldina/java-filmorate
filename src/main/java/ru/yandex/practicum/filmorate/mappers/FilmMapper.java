package ru.yandex.practicum.filmorate.mappers;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

public class FilmMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet rowSet, int rowNum) throws SQLException {
        //Добавила жанры в маппеер, чтобы получать готовый фильм одним запросом из базы
        SortedSet<Genre> genres = new TreeSet<>(Comparator.comparingInt(Genre::getId));
        //массивы номера и имени жанров (или жанра) из бд
        String ids = rowSet.getString("GENRE_ID");
        if (!ids.equals("null")) {
            int[] genreIds = Arrays.stream(ids.split(", ")).mapToInt(Integer::parseInt).toArray();
            String[] genreNames = rowSet.getString("GENRE_N").split(", ");

            for (int i = 0; i < genreIds.length; i++) {
                genres.add(new Genre(genreIds[i], genreNames[i]));
            }
        }
        Film film = new Film(
                rowSet.getString("title"),
                rowSet.getString("description"),
                rowSet.getDate("release_date").toLocalDate(),
                rowSet.getLong("duration"),
                new Mpa(rowSet.getInt("mpa_id"), rowSet.getString("RATING")));
        film.setId(rowSet.getInt("film_id"));
        film.setGenres(genres);
        return film;
    }
}
