package ru.yandex.practicum.filmorate.сontroller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.InvalidUpdateException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//добавление фильма;
//обновление фильма;
//получение всех фильмов.
@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private Map<Integer, Film> films = new HashMap<>();
    private int id = 1;

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        film.setId(id++);
        films.put(film.getId(), film);
        log.info("Фильм успешно добавлен");
        return film;
    }

    @PutMapping
    public Film putFilm(@Valid @RequestBody Film film) {
        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            log.info("Фильм успешно добавлен");
        } else {
            throw new InvalidUpdateException("Taкого фильма нет в списке");
        }
        return film;
    }

    @GetMapping
    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }

}
