package ru.yandex.practicum.filmorate.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FilmStorage;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Slf4j
@Service
public class InMemoryFilmStorage implements FilmStorage {
    private Map<Integer, Film> films = new HashMap<>();
    private int id = 1;

    public Film addFilm(Film film) {
        film.setId(id++);
        films.put(film.getId(), film);
        log.info("Фильм успешно добавлен");
        return film;
    }

    public Film putFilm(Film film) {
        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            log.info("Фильм успешно добавлен");
        } else {
            throw new EntityNotFoundException("Taкого фильма нет в списке");
        }
        return film;
    }

    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }

    public Film findFilmById(int id) {
        if (films.containsKey(id)) {
            return films.get(id);
        } else {
            throw new EntityNotFoundException("Такого фильма нет");
        }
    }
    public List<Film> getPopularFilms(int count) {
        return null;
    }

    @Override
    public void deleteFilm(int id) {

    }

    @Override
    public List<Film> getCommonFilms(Integer userId, Integer friendId) {
        return null;
    }

    public static class MpaDbStorage {
    }
}
