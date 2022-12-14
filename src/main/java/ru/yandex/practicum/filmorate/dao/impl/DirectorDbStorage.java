package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.DirectorStorage;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.mappers.DirectorMapper;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;

@Component("directorDbStorage")
public class DirectorDbStorage implements DirectorStorage {

    private final JdbcTemplate jdbcTemplate;

    public DirectorDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Director createDirector(Director director) {
        jdbcTemplate.update("INSERT INTO DIRECTORS (DIRECTOR_ID, NAME) VALUES ( ?, ? )"
                , director.getId(), director.getName());

        return director;
    }

    @Override
    public Director updateDirector(Director director) {
        Integer rowCount = jdbcTemplate.update("UPDATE DIRECTORS SET NAME = ? WHERE DIRECTOR_ID = ?"
                , director.getName(), director.getId());
        if (rowCount < 1) {
            return null;
        } else {
            return director;
        }
    }

    @Override
    public Collection<Director> getAllDirectors() {
        return jdbcTemplate.query("SELECT * FROM DIRECTORS", new DirectorMapper());
    }

    @Override
    public Director getDirectorById(Integer id) {
        return jdbcTemplate.queryForObject("SELECT * FROM DIRECTORS WHERE DIRECTOR_ID = ?"
                , new DirectorMapper(), id);
    }

    @Override
    public void deleteDirectorById(Integer id) {
        Integer dirCount = jdbcTemplate.update("DELETE FROM FILM_DIRECTORS WHERE DIRECTOR_ID = ?", id);
        Integer dirFilmCount = jdbcTemplate.update("DELETE FROM DIRECTORS WHERE DIRECTOR_ID = ?", id);
        if ((dirCount < 1) || (dirFilmCount < 1)) {
            throw new EntityNotFoundException("Такого режиссера нет");
        }
    }
}
