package ru.yandex.practicum.filmorate.storages;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.dao.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class FilmStorageTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Qualifier("FilmDbStorage")
    @Autowired
    private FilmStorage filmStorage;

    @Autowired
    private GenreStorage genreStorage;

    @Autowired
    private MpaStorage mpaStorage;
    @Qualifier("UserDbStorage")
    @Autowired
    private UserStorage userStorage;

    @Qualifier("LikeDbStorage")
    @Autowired
    private LikeStorage likeStorage;

    private Film film;
    private Genre genreComedy;
    private Genre genreDrama;
    private Mpa gMpa;
    private Mpa pgMpa;

    @BeforeEach
    public void beforeEach() {
        genreComedy = genreStorage.getGenreById(1);
        genreDrama = genreStorage.getGenreById(2);
        gMpa = mpaStorage.getMpaById(1);
        pgMpa = mpaStorage.getMpaById(2);
        film = new Film("Название", "Описание",
                LocalDate.of(2000, 1, 1), 30L, gMpa);
    }

    @Test
    public void deleteFilm() {
        int id = filmStorage.addFilm(film).getId();
        filmStorage.deleteFilm(id);
        String sqlQuery = "SELECT * FROM films_model WHERE film_id = ?";
        SqlRowSet filmRow = jdbcTemplate.queryForRowSet(sqlQuery, id);
        assertFalse(filmRow.next());
    }

    @Test
    public void getFilmById() {
        filmStorage.addFilm(film);
        Film film = filmStorage.findFilmById(1);
        assertEquals(1, film.getId());
    }

    @Test
    public void getAllFilms() {
        filmStorage.addFilm(film);
        filmStorage.addFilm(film);
        filmStorage.addFilm(film);
        assertEquals(3, filmStorage.getFilms().size());
    }

    @Test
    public void getMostLikedFilms() {
        User firstUser = userStorage.addUser(new User("e5k4p3@gmail.com", "e5k4p3", "e5k4p3",
                LocalDate.of(1995, 7, 11)));
        User secondUser = userStorage.addUser(new User("mulenas@gmail.com", "Mulenas", "Mulenas",
                LocalDate.of(1995, 7, 11)));
        Film secondFilm = new Film("Второй", "Описание второго",
                LocalDate.of(1999, 8, 15), 50L, gMpa);
        Film thirdFilm = new Film("Третий", "Описание третьего",
                LocalDate.of(2007, 4, 7), 50L, pgMpa);
        filmStorage.addFilm(film);
        int secondFilmId = filmStorage.addFilm(secondFilm).getId();
        int thirdFilmId = filmStorage.addFilm(thirdFilm).getId();
        likeStorage.addLike(thirdFilmId, firstUser.getId());
        likeStorage.addLike(thirdFilmId, secondUser.getId());
        likeStorage.addLike(secondFilmId, firstUser.getId());
        List<Film> topFilms = filmStorage.getPopularFilms(10);
        System.out.println(topFilms.size());
        System.out.println(topFilms);
        assertEquals("Третий", topFilms.get(0).getName());
        assertEquals("Второй", topFilms.get(1).getName());
        assertEquals("Название", topFilms.get(2).getName());
    }

    private Film getFilmFromRow(SqlRowSet filmRow) {
        Film film = new Film(filmRow.getString("title"),
                filmRow.getString("description"),
                filmRow.getDate("release_date").toLocalDate(),
                filmRow.getLong("duration"),
                getFilmMpa(filmRow));
        film.setId(filmRow.getInt("film_id"));
        film.setGenres(getFilmGenres(film.getId()));
        return film;
    }

    private Mpa getFilmMpa(SqlRowSet filmRow) {
        String sqlQuery = "SELECT * FROM mpa_dictionary WHERE mpa_id = ?";
        SqlRowSet mpaRow = jdbcTemplate.queryForRowSet(sqlQuery, filmRow.getInt("mpa_id"));
        return new Mpa(mpaRow.getInt("mpa_id"), mpaRow.getString("rating"));
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

    private Genre getGenreFromRow(SqlRowSet genreRow) {
        return new Genre(genreRow.getInt("genre_id"),
                genreRow.getString("genre_name"));
    }
}