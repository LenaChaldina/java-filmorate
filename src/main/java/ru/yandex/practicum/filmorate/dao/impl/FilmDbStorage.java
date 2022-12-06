package ru.yandex.practicum.filmorate.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.FilmStorage;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.mappers.GenreMapper;
import ru.yandex.practicum.filmorate.mappers.MpaMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.*;

@Slf4j
@Component("FilmDbStorage")
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film addFilm(Film film) {
        String sqlQuery = "INSERT INTO films_model(title, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?,?)";
        jdbcTemplate.update(sqlQuery,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId());
        film.setId(getFilmId(film));

        addFilmGenres(film);
        log.info("Фильм успешно добавлен");
        return findFilmById(film.getId());
    }

    public void deleteFilm(int id) {
        if (getSqlRowSetByFilmId(id).next()) {
            removeFilmGenres(id);
            removeFilmLikes(id);
            String filmSqlQuery = "DELETE FROM films_model WHERE film_id = ?";
            jdbcTemplate.update(filmSqlQuery, id);
            log.info("Фильм с id " + id + " удален.");
        } else {
            throw new EntityNotFoundException("Фильм с id " + id + " не найден.");
        }
    }

    @Override
    public Film putFilm(Film film) {
        //если фильм с таким id найден - обновляю все поля
        if (getFilmsSqlRowSet(film.getId()).next()) {
            String sqlQuery = "UPDATE films_model SET title = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE film_id = ?";
            jdbcTemplate.update(sqlQuery,
                    film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getMpa().getId(), film.getId());
            removeFilmGenres(film.getId());
            addFilmGenres(film);
            film.setGenres(getFilmGenres(film.getId()));
            log.info("Фильм успешно обновлен");
            return findFilmById(film.getId());
        } else {
            throw new EntityNotFoundException("Фильма с id:" + film.getId() + "нет в базе");
        }
    }

    @Override
    public Film findFilmById(int id) {
        checkFilmIdExists(id);
        Film film = jdbcTemplate.queryForObject("SELECT * FROM films_model WHERE FILM_ID = ?"
                , new FilmMapper(), id);
        Mpa mpa = jdbcTemplate.queryForObject("select mpa_dictionary.mpa_id, mpa_dictionary.rating from mpa_dictionary where mpa_id =?", new MpaMapper(), film.getMpa().getId());
        film.setMpa(mpa);
        List<Genre> genres = jdbcTemplate.query("SELECT G2.* FROM films_genres G1 inner JOIN genre_dictionary G2 on G2.genre_id = g1.genre_id WHERE g1.film_id=?;"
                , new GenreMapper(), id);
        SortedSet<Genre> genresSet = new TreeSet<>(Comparator.comparingInt(Genre::getId));
        genresSet.addAll(genres);
        film.setGenres(genresSet);
        return film;
    }

    private void checkFilmIdExists(int id) {
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("select * from films_model where film_id = ? ", id);
        if (!filmRows.next()) {
            throw new EntityNotFoundException("Фильм с id " + id + " не найден.");
        }
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        //самый залайканые фильмы
        return jdbcTemplate.query("SELECT fm.*, COUNT(fl.like_id) FROM films_model AS fm " +
                "LEFT OUTER JOIN films_likes AS fl on fm.film_id = fl.film_id " +
                "GROUP BY fm.film_id ORDER BY COUNT(fl.like_id) DESC LIMIT ?", new FilmMapper(), count);
    }

    public List<Film> getListFilms() {
        List<Film> films = new ArrayList<>();
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("SELECT film_id from films_model order by film_id");
        while (filmRows.next()) {
            films.add(findFilmById(filmRows.getInt("film_id")));
        }
        return films;
    }

    private SqlRowSet getFilmsSqlRowSet(int id) {
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("select * from films_model where film_id = ? ", id);
        return filmRows;
    }

    private void removeFilmGenres(int id) {
        String sqlQuery = "DELETE FROM films_genres WHERE film_id = ?";
        jdbcTemplate.update(sqlQuery, id);
    }

    private SqlRowSet getSqlRowSetByFilmId(int id) {
        String sqlQuery = "SELECT * FROM films_model WHERE film_id = ?";
        return jdbcTemplate.queryForRowSet(sqlQuery, id);
    }

    private Genre getGenreFromRow(SqlRowSet genreRow) {
        return new Genre(genreRow.getInt("genre_id"),
                genreRow.getString("genre_name"));
    }

    private void addFilmGenres(Film film) {
        //если такой жанр существует
        checkGenreIdExistence(film);
        String sqlQuery = "insert into films_genres(film_id, genre_id) values(?,?)";
        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update(sqlQuery, film.getId(), genre.getId());
        }
    }

    private SortedSet<Genre> getFilmGenres(int id) {
        SortedSet<Genre> filmGenres = new TreeSet<>(Comparator.comparingInt(Genre::getId));


        String sqlQuery = "SELECT * FROM genre_dictionary WHERE genre_id IN " +
                "(SELECT genre_id FROM FILMS_GENRES WHERE film_id = ?)";
        SqlRowSet genreRows = jdbcTemplate.queryForRowSet(sqlQuery, id);
        while (genreRows.next()) {
            filmGenres.add(getGenreFromRow(genreRows));
        }
        return filmGenres;
    }

    private void removeFilmLikes(int id) {
        String sqlQuery = "DELETE FROM FILMS_LIKES WHERE film_id = ?";
        jdbcTemplate.update(sqlQuery, id);
    }

    private void checkGenreIdExistence(Film film) {
        String sqlQuery = "SELECT * FROM genre_dictionary WHERE genre_id = ?";
        for (Genre genre : film.getGenres()) {
            if (!jdbcTemplate.queryForRowSet(sqlQuery, genre.getId()).next()) {
                throw new EntityNotFoundException("Жанр с id " + genre.getId() + " не найден.");
            }
        }
    }

    private Integer getFilmId(Film film) {
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("select * from films_model where title = ? " +
                        "and description = ? and release_date = ? and duration = ? and mpa_id = ?",
                film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getMpa().getId());
        if (filmRows.next()) {
            Film dbFilm = new Film(filmRows.getString("title"), filmRows.getString("description"),
                    filmRows.getDate("release_date").toLocalDate(), filmRows.getLong("duration"),
                    getMpa(filmRows.getInt("mpa_id")));
            dbFilm.setId(filmRows.getInt("film_id"));
            return dbFilm.getId();
        } else {
            return null;
        }
    }

    private Mpa getMpa(int id) {
        SqlRowSet mpaRows = jdbcTemplate.queryForRowSet("select rating from mpa_dictionary where mpa_id = ?", id);
        if (mpaRows.next()) {
            Mpa dbMpa = new Mpa(id, mpaRows.getString("rating"));
            return dbMpa;
        } else {
            return null;
        }
    }
}
