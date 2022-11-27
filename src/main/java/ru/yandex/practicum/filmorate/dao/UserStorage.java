package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    public User addUser(User user);

    public User putUser(User user);

    public List<User> getUsers();

    public User findUserById(int id);
}
