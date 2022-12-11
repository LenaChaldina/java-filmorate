package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.GenreStorage;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.ArrayList;
import java.util.List;

@Repository
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Genre getGenreById(int id) {
        String sqlQuery = "SELECT * FROM genre_dictionary WHERE genre_id = ?";
        SqlRowSet genreRow = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (genreRow.next()) {
            return getGenreFromRow(genreRow);
        } else {
            throw new EntityNotFoundException("Жанр с id " + id + " не найден.");
        }
    }

    @Override
    public List<Genre> getAllGenres() {
        List<Genre> allGenre = new ArrayList<>();
        String sqlQuery = "SELECT * FROM genre_dictionary";
        SqlRowSet genreRow = jdbcTemplate.queryForRowSet(sqlQuery);
        while (genreRow.next()) {
            allGenre.add(getGenreFromRow(genreRow));
        }
        return allGenre;
    }

    private Genre getGenreFromRow(SqlRowSet genreRow) {
        return new Genre(genreRow.getInt("genre_id"),
                genreRow.getString("genre_name"));
    }
}
