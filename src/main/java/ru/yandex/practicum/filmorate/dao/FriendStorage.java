package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface FriendStorage {
    void addFriend(int userId, int friendId);
    void deleteFriend(int userId, int friendId);

    List<User> getFriends(int id);

    List<User> getCommonFriends(int userId, int otherId);
}
