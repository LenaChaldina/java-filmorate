package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;

public interface DirectorStorage {
    Director createDirector(Director director);

    Director updateDirector(Director director);

    Collection<Director> getAllDirectors();

    Director getDirectorById(Integer id);

    void deleteDirectorById(Integer id);
}