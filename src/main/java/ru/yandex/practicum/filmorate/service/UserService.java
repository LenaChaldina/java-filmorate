package ru.yandex.practicum.filmorate.service;

//Создайте UserService, который будет отвечать за такие операции с пользователями,
// как добавление в друзья, удаление из друзей, вывод списка общих друзей.
// Пока пользователям не надо одобрять заявки в друзья — добавляем сразу.
// То есть если Лена стала другом Саши, то это значит, что Саша теперь друг Лены.

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FeedStorage;
import ru.yandex.practicum.filmorate.dao.FriendStorage;
import ru.yandex.practicum.filmorate.enums.EventTypeEnum;
import ru.yandex.practicum.filmorate.enums.OperationTypeEnum;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.dao.UserStorage;

import java.util.List;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private FriendStorage friendStorage;
    private final FeedStorage feedStorage;

    @Autowired
    public UserService(@Qualifier("UserDbStorage") UserStorage userStorage, @Qualifier("FriendDbStorage") FriendStorage friendStorage, FeedStorage feedStorage) {
        this.userStorage = userStorage;
        this.friendStorage = friendStorage;
        this.feedStorage = feedStorage;
    }

    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    public User putUser(User user) {
        return userStorage.putUser(user);
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public User findUserById(int id) {
        return userStorage.findUserById(id);
    }

    public void addFriend(int userId, int friendId) {
        friendStorage.addFriend(userId, friendId);
        feedStorage.addFeedEvent(userId, friendId, EventTypeEnum.FRIEND, OperationTypeEnum.ADD);
    }

    public void deleteFriend(int userId, int friendId) {
        if (userStorage.findUserById(userId) == null) {
            throw new EntityNotFoundException("Такого юзера " + userId + " нет");
        }
        if (userStorage.findUserById(friendId) == null) {
            throw new EntityNotFoundException("Такого друга " + friendId + "нет");
        } else {
            feedStorage.addFeedEvent(userId, friendId, EventTypeEnum.FRIEND, OperationTypeEnum.REMOVE);
            friendStorage.deleteFriend(userId, friendId);
        }
    }

    public List<User> getFriends(int id) {
        return friendStorage.getFriends(id);
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        return friendStorage.getCommonFriends(userId, otherId);
    }
}
