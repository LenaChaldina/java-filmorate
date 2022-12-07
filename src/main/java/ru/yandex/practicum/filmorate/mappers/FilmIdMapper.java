package ru.yandex.practicum.filmorate.mappers;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FilmIdMapper implements RowMapper<Integer> {

    @Override
    public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
        return rs.getInt("FILM_ID");
    }
}
