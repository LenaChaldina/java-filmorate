package ru.yandex.practicum.filmorate.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.FilmStorage;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.mappers.*;
import ru.yandex.practicum.filmorate.model.Director;
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
        addDirectorForFilm(film);
        log.info("Фильм успешно добавлен");
        return findFilmById(film.getId());
    }

    public void deleteFilm(int id) {
        if (getSqlRowSetByFilmId(id).next()) {
            removeFilmGenres(id);
            removeFilmLikes(id);
            removeFilmDirector(id);
            removeFilmReviews(id);
            String filmSqlQuery = "DELETE FROM films_model WHERE film_id = ?";
            jdbcTemplate.update(filmSqlQuery, id);
            log.info("Фильм с id " + id + " удален.");
        } else {
            throw new EntityNotFoundException("Фильм с id " + id + " не найден.");
        }
    }

    @Override
    public List<Film> getPopularFilmsWithFilter(int limit, int genreId, int year) {
        List<Film> films = new ArrayList<>();
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("select films_model.film_id\n" +
                "from films_model left join films_likes on films_model.film_id = films_likes.film_id\n" +
                "LEFT JOIN films_genres on films_model.film_id = films_genres.film_id\n" +
                "WHERE (films_genres.genre_id = ? OR 0 = ?)\n" +
                "and (EXTRACT(YEAR from films_model.release_date) = ? or 0 = ?)\n" +
                "GROUP by films_model.film_id\n" +
                "ORDER by COUNT(films_likes.like_id) DESC, films_model.film_id\n" +
                "LIMIT ?;", genreId, genreId, year, year, limit);
        while (filmRows.next()) {
            films.add(findFilmById(filmRows.getInt("film_id")));
        }
        return films;
    }

    @Override
    public Film putFilm(Film film) {
        //если фильм с таким id найден - обновляю все поля
        jdbcTemplate.update("DELETE FROM FILM_DIRECTORS WHERE FILM_ID = ?", film.getId());

        if (getFilmsSqlRowSet(film.getId()).next()) {
            String sqlQuery = "UPDATE films_model SET title = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE film_id = ?";
            jdbcTemplate.update(sqlQuery,
                    film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getMpa().getId(), film.getId());
            removeFilmGenres(film.getId());
            addFilmGenres(film);
            addDirectorForFilm(film);
            //film.setGenres(getFilmGenres(film.getId()));
            log.info("Фильм успешно обновлен");
            return findFilmById(film.getId());
        } else {
            throw new EntityNotFoundException("Фильма с id:" + film.getId() + "нет в базе");
        }
    }

    @Override
    public Film findFilmById(int id) {
        if (checkFilmIdExists(id)) {
            throw new EntityNotFoundException("Фильм с id " + id + " не найден.");
        }
        Film film = jdbcTemplate.queryForObject("SELECT * FROM films_model WHERE FILM_ID = ?"
                , new FilmMapper(), id);
        Mpa mpa = jdbcTemplate.queryForObject("select mpa_dictionary.mpa_id, mpa_dictionary.rating from mpa_dictionary where mpa_id =?", new MpaMapper(), film.getMpa().getId());
        film.setMpa(mpa);
        List<Genre> genres = jdbcTemplate.query("SELECT G2.* FROM films_genres G1 inner JOIN genre_dictionary G2 on G2.genre_id = g1.genre_id WHERE g1.film_id=?;"
                , new GenreMapper(), id);
        List<Director> directorList = jdbcTemplate.query("SELECT F.DIRECTOR_ID, D.NAME " +
                        "FROM FILM_DIRECTORS F LEFT JOIN DIRECTORS D on D.DIRECTOR_ID = F.DIRECTOR_ID WHERE FILM_ID = ?"
                , new DirectorMapper(), id);
        SortedSet<Genre> genresSet = new TreeSet<>(Comparator.comparingInt(Genre::getId));

        genresSet.addAll(genres);
        film.setGenres(genresSet);
        film.setDirectors(directorList);
        return film;
    }

    public boolean checkFilmIdExists(int id) {
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("select * from films_model where film_id = ? ", id);
        return !filmRows.next();
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
        return jdbcTemplate.queryForRowSet("select * from films_model where film_id = ? ", id);
    }

    private void removeFilmGenres(int id) {
        String sqlQuery = "DELETE FROM films_genres WHERE film_id = ?";
        jdbcTemplate.update(sqlQuery, id);
    }

    private void removeFilmReviews(int id) {
        String sqlQuery = "DELETE FROM REVIEWS WHERE film_id = ?";
        jdbcTemplate.update(sqlQuery, id);
    }

    private void removeFilmDirector(int id) {
        jdbcTemplate.update("DELETE FROM FILM_DIRECTORS WHERE FILM_ID = ?", id);
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

    private void addDirectorForFilm(Film film) {
        if (film.getDirectors() != null) {
            for (Director director : film.getDirectors()) {
                jdbcTemplate.update("INSERT INTO FILM_DIRECTORS (FILM_ID, DIRECTOR_ID) VALUES (?, ?)"
                        , film.getId(), director.getId());
            }
        }
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
            return new Mpa(id, mpaRows.getString("rating"));
        } else {
            return null;
        }
    }

    public Collection<Film> getDirectorFilmSortedByLike(Integer directorId) {
        Comparator<Film> comparator = Comparator.comparing(Film::getId);
        Collection<Film> sortDirectorFilmListByLike = new ArrayList<>();
        Collection<Integer> filmIdList = jdbcTemplate.query("SELECT FILMS_LIKES.film_id, COUNT(FILMS_LIKES.like_id)\n" +
                "FROM DIRECTORS INNER JOIN FILM_DIRECTORS on DIRECTORS.director_id = FILM_DIRECTORS.director_id\n" +
                "INNER JOIN FILMS_LIKES ON FILM_DIRECTORS.film_id = FILMS_LIKES.film_id\n" +
                "WHERE DIRECTORS.director_id = ? \n" +
                "GROUP BY FILMS_LIKES.film_id\n" +
                "ORDER BY COUNT(FILMS_LIKES.like_id)", new FilmIdMapper(), directorId);
        if (filmIdList.size() == 0) {
            return getFilmListForDirector(directorId, comparator);
        }
        for (Integer id : filmIdList) {
            sortDirectorFilmListByLike.add(findFilmById(id));
        }
        return sortDirectorFilmListByLike;
    }

    public Collection<Film> getDirectorFilmSortedByYear(Integer directorId) {
        Comparator<Film> comparator = Comparator.comparing(Film::getReleaseDate)
                .thenComparing(Film::getId);
        return getFilmListForDirector(directorId, comparator);
    }

    private Collection<Film> getFilmListForDirector(Integer directorId, Comparator<Film> comparator) {
        Collection<Integer> filmIdList = jdbcTemplate.query("SELECT * FROM FILM_DIRECTORS " +
                "WHERE DIRECTOR_ID = ?", new FilmIdMapper(), directorId);
        Collection<Film> directorFilmList = new ArrayList<>();
        for (Integer id : filmIdList) {
            directorFilmList.add(findFilmById(id));
        }
        Collection<Film> sortedFilmList = new TreeSet<>(comparator);
        sortedFilmList.addAll(directorFilmList);
        return sortedFilmList;
    }
}
