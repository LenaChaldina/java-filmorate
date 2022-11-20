package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private FilmStorage filmStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Film putFilm(Film film) {
        return filmStorage.putFilm(film);
    }

    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public void putLike(int id, int userId) {
        if (filmStorage.findFilmById(id) == null) {
            throw new EntityNotFoundException("Такого фильма " + id + " нет");
        }
        if (filmStorage.findFilmById(userId) == null) {
            throw new EntityNotFoundException("Такого юзера " + userId + "нет");
        } else {
            filmStorage.findFilmById(id).addLike(userId);
        }
    }

    public void deleteLike(int id, int userId) {
        if (filmStorage.findFilmById(id) == null) {
            throw new EntityNotFoundException("Такого фильма " + id + " нет");
        }
        if (filmStorage.findFilmById(userId) == null) {
            throw new EntityNotFoundException("Такого юзера " + userId + "нет");
        } else {
            filmStorage.findFilmById(id).removeLike(userId);
        }
    }

    public List<Film> getPopularFilms(int count) {
        return new ArrayList<>(filmStorage.getFilms().stream().sorted(Comparator.comparingInt(Film::getCountLikes).reversed()).limit(count)
                .collect(Collectors.toList()));
    }

    public Film findFilmById(int id) {
        return filmStorage.findFilmById(id);
    }
}

