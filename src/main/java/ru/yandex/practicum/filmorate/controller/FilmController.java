package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        return filmService.addFilm(film);
    }

    @PutMapping
    public Film putFilm(@Valid @RequestBody Film film) {
        return filmService.putFilm(film);
    }

    @GetMapping
    public List<Film> getFilms() {
        return filmService.getFilms();
    }

    //PUT /films/{id}/like/{userId} — пользователь ставит лайк фильму.
    @PutMapping("/{id}/like/{userId}")
    public void putLike(@PathVariable("id") int id, @PathVariable("userId") int userId) {
        filmService.putLike(id, userId);
    }

    //DELETE /films/{id}/like/{userId} — пользователь удаляет лайк.
    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable("id") int id, @PathVariable("userId") int userId) {
        filmService.deleteLike(id, userId);
    }

    //Возвращает указанный список самых популярных фильмов указанного жанра за нужный год.
    //Фильтрация должна быть по двум параметрам.
    //1. По жанру.
    //2. За указанный год.
    // API
    //`GET /films/popular?count={limit}&genreId={genreId}&year={year}`
    @GetMapping("/popular")
    public List<Film> getPopularFilmsWithFilter(
            @RequestParam(value = "count", defaultValue = "10", required = false) Integer count,
            @RequestParam(value = "genreId", defaultValue = "0", required = false) Integer genreId,
            @RequestParam(value = "year", defaultValue = "0", required = false) Integer year) {

        return filmService.getPopularFilmsWithFilter(count, genreId, year);
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable("id") int id) {
        return filmService.findFilmById(id);
    }

    @DeleteMapping("/{filmId}")
    public void deleteFilm(@PathVariable int filmId) {
        filmService.deleteFilm(filmId);
    }

    @GetMapping("/director/{directorId}")
    public Collection<Film> getFilmsDirectorBySort(@PathVariable Integer directorId
            , @RequestParam(value = "sortBy") String sort) throws SQLException {
        if(sort.equals("likes")) {
            return filmService.getDirectorFilmSortedByLike(directorId);
        } else {
            return filmService.getDirectorFilmSortedByYear(directorId);
        }
    }

    @GetMapping("/common")
    public List<Film> getCommonFilms(@RequestParam Integer userId, Integer friendId) {
        return filmService.getCommonFilms(userId, friendId);
    }
}
