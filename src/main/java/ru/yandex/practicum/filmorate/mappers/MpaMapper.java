package ru.yandex.practicum.filmorate.mappers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import ru.yandex.practicum.filmorate.model.Film;
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
