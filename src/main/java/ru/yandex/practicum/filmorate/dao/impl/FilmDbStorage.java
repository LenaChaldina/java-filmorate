package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.FilmStorage;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.mappers.FilmIdMapper;
import ru.yandex.practicum.filmorate.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Repository("FilmDbStorage")
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
        return film;
    }

    public void deleteFilm(int id) {
        removeFilmGenres(id);
        removeFilmLikes(id);
        removeFilmDirector(id);
        removeFilmReviews(id);
        String filmSqlQuery = "DELETE FROM films_model WHERE film_id = ?";
        jdbcTemplate.update(filmSqlQuery, id);
    }

    @Override
    public List<Film> getPopularFilmsWithFilter(int limit, int genreId, int year) {
        List<Film> filmRows = jdbcTemplate.query("select FM.FILM_ID, FM.TITLE, FM.DESCRIPTION, FM.release_date,FM.DURATION, FM.MPA_ID, MD.RATING,\n" +
                "       TRIM(BOTH ']' from TRIM(BOTH '[' FROM ARRAY_AGG(GD.GENRE_ID))) as GENRE_ID ,\n" +
                "       TRIM(BOTH ']' from TRIM(BOTH '[' FROM TRIM(BOTH '}' from TRIM(BOTH '{' FROM ARRAY_AGG(GD.GENRE_NAME))))) as GENRE_N" +
                ", 'null' as DIRECTOR_ID, 'null' as DIRECTOR_N \n" +
                "from FILMS_MODEL FM\n" +
                "                inner join MPA_DICTIONARY MD on FM.MPA_ID = MD.MPA_ID\n" +
                "                left join films_likes on FM.film_id = films_likes.film_id\n" +
                "                LEFT JOIN films_genres on FM.film_id = films_genres.film_id\n" +
                "                LEFT JOIN GENRE_DICTIONARY GD on FILMS_GENRES.GENRE_ID = GD.GENRE_ID\n" +
                "                WHERE (films_genres.genre_id = ? OR 0 = ?)\n" +
                "                and (EXTRACT(YEAR from FM.release_date) = ? or 0 = ?)\n" +
                "                GROUP by FM.film_id, FM.TITLE, FM.DESCRIPTION, FM.release_date,FM.DURATION, FM.MPA_ID, MD.RATING\n" +
                "                ORDER by COUNT(films_likes.like_id) DESC, FM.film_id\n" +
                "                LIMIT ?;", new FilmMapper(), genreId, genreId, year, year, limit);

        return filmRows;
    }

    @Override
    public Film putFilm(Film film) {
        //если фильм с таким id найден - обновляю все поля
        jdbcTemplate.update("DELETE FROM FILM_DIRECTORS WHERE FILM_ID = ?", film.getId());
        String sqlQuery = "UPDATE films_model SET title = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE film_id = ?";
        jdbcTemplate.update(sqlQuery,
                film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getMpa().getId(), film.getId());
        removeFilmGenres(film.getId());
        addFilmGenres(film);
        addDirectorForFilm(film);
        Film film1 = findFilmById(film.getId());
        return film1;
    }

    @Override
    public Film findFilmById(int id) {
        if (checkFilmIdExists(id)) {
            throw new EntityNotFoundException("Фильм с id " + id + " не найден.");
        }
        Film film = jdbcTemplate.queryForObject("select FM.FILM_ID,\n" + "FM.TITLE,\n" + "FM.DESCRIPTION,\n" +
                        "FM.release_date,\n" + "FM.DURATION,\n" + "FM.MPA_ID,\n" + "MD.RATING,\n" + "\n" +
                        "TRIM(BOTH ']' from TRIM(BOTH '[' FROM ARRAY_AGG(GD.GENRE_ID)))                             as GENRE_ID,\n" +
                        "TRIM(BOTH ']' from TRIM(BOTH '[' FROM\n" + "TRIM(BOTH '}' from TRIM(BOTH '{' FROM ARRAY_AGG(GD.GENRE_NAME))))) as GENRE_N,\n" + "\n" +
                        "TRIM(BOTH ']' from TRIM(BOTH '[' FROM ARRAY_AGG(FD.DIRECTOR_ID)))                          as DIRECTOR_ID,\n" +
                        "TRIM(BOTH ']' from TRIM(BOTH '[' FROM\n" + "                               TRIM(BOTH '}' from TRIM(BOTH '{' FROM ARRAY_AGG(D.NAME))))) as DIRECTOR_N\n" + "from FILMS_MODEL FM\n" + "inner join MPA_DICTIONARY MD\n" +
                        "                    on FM.MPA_ID = MD.MPA_ID\n" + "left join FILMS_GENRES FG\n" +
                        "                   on FM.FILM_ID = FG.FILM_ID\n" + "left join GENRE_DICTIONARY GD on FG.GENRE_ID = GD.GENRE_ID\n" +
                        "left join FILM_DIRECTORS FD on FM.FILM_ID = FD.FILM_ID\n" + "left join DIRECTORS D on D.DIRECTOR_ID = FD.DIRECTOR_ID\n" +
                        "group by FM.FILM_ID, FM.TITLE, FM.MPA_ID, MD.RATING\n" + "having FM.FILM_ID = ?;"
                , new FilmMapper(), id);
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

    public SqlRowSet getFilmsSqlRowSet(int id) {
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

    public SqlRowSet getSqlRowSetByFilmId(Integer id) {
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
        Collection<Film> films = new ArrayList<>();
        Collection<Integer> filmIdList = jdbcTemplate.query("SELECT FILM_DIRECTORS.film_id, COUNT(FILMS_LIKES.like_id) " +
                "FROM DIRECTORS\n" +
                "INNER JOIN FILM_DIRECTORS on DIRECTORS.director_id = FILM_DIRECTORS.director_id\n" +
                "LEFT JOIN FILMS_LIKES ON FILM_DIRECTORS.film_id = FILMS_LIKES.film_id\n" +
                "WHERE DIRECTORS.director_id = ?\n" +
                "GROUP BY FILM_DIRECTORS.film_id\n" +
                "ORDER BY COUNT(FILMS_LIKES.like_id) DESC", new FilmIdMapper(), directorId);

        for (Integer id : filmIdList) {
            films.add(findFilmById(id));
        }
        return films;
    }

    public Collection<Film> getDirectorFilmSortedByYear(Integer directorId) {
        Collection<Film> films = new ArrayList<>();
        Collection<Integer> filmIdList = jdbcTemplate.query("SELECT FILM_DIRECTORS.film_id\n" +
                "FROM DIRECTORS\n" +
                "INNER JOIN FILM_DIRECTORS on DIRECTORS.director_id = FILM_DIRECTORS.director_id\n" +
                "INNER JOIN FILMS_MODEL ON FILM_DIRECTORS.film_id = FILMS_MODEL.FILM_ID\n" +
                "WHERE DIRECTORS.director_id = ?\n" +
                "GROUP BY FILM_DIRECTORS.film_id\n" +
                "ORDER BY FILMS_MODEL.RELEASE_DATE", new FilmIdMapper(), directorId);

        for (Integer id : filmIdList) {
            films.add(findFilmById(id));
        }
        return films;
    }

    @Override
    public List<Film> getCommonFilms(Integer userId, Integer friendId) {
        List<Film> films = new ArrayList<>();
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("" +
                "select user_likes.FILM_ID from FILMS_LIKES as user_likes " +
                "inner join FILMS_LIKES as frien_likes on frien_likes.FILM_ID=user_likes.FILM_ID " +
                "where user_likes.USER_ID = ? and frien_likes.USER_ID = ? " +
                "group by frien_likes.FILM_ID " +
                "order by count(frien_likes.like_id) desc", userId, friendId);
        while (sqlRowSet.next()) {
            films.add(findFilmById(sqlRowSet.getInt("film_id")));
        }
        return films;
    }

    @Override
    public List<Film> searchFilm(String query, List<String> searchBy) {
        List<Film> films = new ArrayList<>();
        query = "%" + query.toLowerCase() + "%";
        if (searchBy.contains("director") && searchBy.contains("title")) {
            SqlRowSet searchByTitle = jdbcTemplate.queryForRowSet("" +
                    "select films.FILM_ID from FILMS_MODEL as films " +
                    "left join FILM_DIRECTORS as film_dir on film_dir.FILM_ID = films.FILM_ID " +
                    "left join DIRECTORS dir on dir.DIRECTOR_ID = film_dir.DIRECTOR_ID " +
                    "left join FILMS_LIKES likes on likes.FILM_ID = films.FILM_ID " +
                    "where lower(TITLE) like ? or lower(NAME) like ? " +
                    "group by films.FILM_ID " +
                    "order by count(likes.like_id) desc", query, query);
            while (searchByTitle.next()) {
                films.add(findFilmById(searchByTitle.getInt("film_id")));
            }
            return films;
        }
        if (searchBy.contains("title")) {
            SqlRowSet searchByTitle = jdbcTemplate.queryForRowSet("" +
                    "select films.FILM_ID from FILMS_MODEL as films " +
                    "left join FILMS_LIKES likes on likes.FILM_ID = films.FILM_ID " +
                    "where lower(TITLE) like ? " +
                    "group by films.FILM_ID " +
                    "order by count(likes.like_id) desc", query);
            while (searchByTitle.next()) {
                films.add(findFilmById(searchByTitle.getInt("film_id")));
            }
        }
        if (searchBy.contains("director")) {
            SqlRowSet searchByTitle = jdbcTemplate.queryForRowSet("" +
                    "select films.FILM_ID from DIRECTORS as dir " +
                    "left join FILM_DIRECTORS as film_dir on film_dir.DIRECTOR_ID = dir.DIRECTOR_ID " +
                    "left join FILMS_MODEL films on film_dir.FILM_ID = films.FILM_ID " +
                    "inner join FILMS_LIKES likes on likes.FILM_ID = films.FILM_ID " +
                    "where lower(NAME) like ? " +
                    "group by films.FILM_ID " +
                    "order by count(likes.like_id) desc", query);
            while (searchByTitle.next()) {
                films.add(findFilmById(searchByTitle.getInt("film_id")));
            }
        }
        return films;
    }
}
