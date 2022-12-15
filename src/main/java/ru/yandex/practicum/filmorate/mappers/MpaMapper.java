package ru.yandex.practicum.filmorate.mappers;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MpaMapper implements RowMapper<Mpa> {
    @Override
    public Mpa mapRow(ResultSet mpaRow, int rowNum) throws SQLException {
        return new Mpa(mpaRow.getInt("mpa_id"),
                mpaRow.getString("rating"));
    }
}
