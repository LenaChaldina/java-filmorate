package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.InvalidUpdateException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class FilmService {
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
            throw new InvalidUpdateException("Taкого фильма нет в списке");
        }
        return film;
    }

    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }

}
