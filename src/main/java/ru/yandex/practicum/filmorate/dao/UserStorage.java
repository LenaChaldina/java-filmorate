package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    User addUser(User user);

    User putUser(User user);

    List<User> getUsers();

    User findUserById(int id);

    List<Film> getFilmsRecommendations(int userId);
}
