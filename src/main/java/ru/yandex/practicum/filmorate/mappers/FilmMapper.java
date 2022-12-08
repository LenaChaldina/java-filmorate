package ru.yandex.practicum.filmorate.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;


import javax.swing.tree.TreePath;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FilmMapper implements RowMapper<Film> {

    @Override
    public Film mapRow(ResultSet rowSet, int rowNum) throws SQLException {
        Film film = new Film(
                rowSet.getString("title"),
                rowSet.getString("description"),
                rowSet.getDate("release_date").toLocalDate(),
                rowSet.getLong("duration"),
                new Mpa(rowSet.getInt("mpa_id"), null));
        film.setId(rowSet.getInt("film_id"));
        return film;
    }
}
