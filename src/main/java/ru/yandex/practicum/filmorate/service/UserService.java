package ru.yandex.practicum.filmorate.service;

//Создайте UserService, который будет отвечать за такие операции с пользователями,
// как добавление в друзья, удаление из друзей, вывод списка общих друзей.
// Пока пользователям не надо одобрять заявки в друзья — добавляем сразу.
// То есть если Лена стала другом Саши, то это значит, что Саша теперь друг Лены.

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FeedStorage;
import ru.yandex.practicum.filmorate.dao.FriendStorage;
import ru.yandex.practicum.filmorate.dao.UserStorage;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.OperationType;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.RequestError;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final FriendStorage friendStorage;
    private final FeedStorage feedStorage;

    private Integer id = 1;

    @Autowired
    public UserService(@Qualifier("UserDbStorage") UserStorage userStorage
            , @Qualifier("FriendDbStorage") FriendStorage friendStorage
            , @Qualifier("FeedDbStorage") FeedStorage feedStorage) {
        this.userStorage = userStorage;
        this.friendStorage = friendStorage;
        this.feedStorage = feedStorage;
    }

    public User addUser(User user) {
        if (user.getId() == null) {
            user.setId(id);
            log.info("Пользователю присвоен id = {} автоматически", user.getId());
            id++;
        }
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
            log.info("Пользователь не указал имени, присвоено значение логина - {}", user.getName());
        }
        log.info("Пользователь {} добавлен в список", user.getName());
        return userStorage.addUser(user);
    }

    public User putUser(User user) {
        if (checkOnContainsUser(user.getId())) {
            if (user.getName() == null) {
                user.setName(user.getLogin());
                log.info("Пользователь не указал имени, присвоено значение логина - {}", user.getName());
            }
            log.info("Информация о пользователь {} обновлена", user.getName());
            return userStorage.putUser(user);
        } else {
            log.warn("Не удалось найти пользователя {} для обновления информации", user.getName());
            throw new EntityNotFoundException("Ошибка. Не удалось найти пользователя для обновления информации");
        }
    }

    public List<User> getUsers() {
        log.info("Получен GET запрос списка пользователей");
        return userStorage.getUsers();
    }

    public User findUserById(int id) {
        if (checkOnContainsUser(id)) {
            log.info("Получен GET запрос пользователя с id = {}", id);
            return userStorage.findUserById(id);
        } else {
            log.warn("Получен GET запрос пользователя с несуществующим id - {}", id);
            throw new EntityNotFoundException("Ошибка. Пользователь с id = " + id + " не найден.");
        }
    }

    public void addFriend(int userId, int friendId) {
        if (!checkOnContainsUser(userId)) {
            log.warn("Не удалось найти пользователя с id = {} ", userId);
            throw new EntityNotFoundException("Ошибка при удалении пользователя из друзей - пользователь не найден");
        }
        if (!checkOnContainsUser(friendId)) {
            log.warn("Не удалось найти пользователя с id = {} ", friendId);
            throw new EntityNotFoundException("Ошибка при удалении пользователя из друзей - пользователь не найден");
        }

        if (containsUserInFriendList(userId, friendId)) {
            log.warn("Не удалось добавить пользователя {} ", userStorage.findUserById(friendId).getName() +
                    " в друзья, так как он уже друг");
            throw new EntityNotFoundException("Ошибка при добавлении пользователя в друзья");
        } else {
            log.info("Пользователи {} и {} подружились", userStorage.findUserById(friendId).getName()
                    , userStorage.findUserById(userId).getName());
            friendStorage.addFriend(userId, friendId);
            if(feedStorage.addFeedEvent(Feed.builder().userId(userId).entityId(friendId).eventType(EventType.FRIEND).operation(OperationType.ADD).build()) != 1) {
                log.warn("Ошибка добавления события userId = {}, entityId = {}, eventType = {}, operation = {} в ленту!",
                        userId,
                        friendId,
                        EventType.FRIEND,
                        OperationType.ADD);
                throw new RequestError( HttpStatus.INTERNAL_SERVER_ERROR,"Событие не добавлено в ленту!");
            } else {
                log.info("Добавлено событие в ленту!");
            }
        }
    }

    public void deleteFriend(int userId, int friendId) {
        if (userStorage.findUserById(userId) == null) {
            throw new EntityNotFoundException("Такого юзера " + userId + " нет");
        }
        if (userStorage.findUserById(friendId) == null) {
            throw new EntityNotFoundException("Такого друга " + friendId + "нет");
        } else {
            if(feedStorage.addFeedEvent(Feed.builder().userId(userId).entityId(friendId).eventType(EventType.FRIEND).operation(OperationType.REMOVE).build()) != 1) {
                log.warn("Ошибка добавления события userId = {}, entityId = {}, eventType = {}, operation = {} в ленту!",
                        userId,
                        friendId,
                        EventType.FRIEND,
                        OperationType.REMOVE);
                throw new RequestError( HttpStatus.INTERNAL_SERVER_ERROR,"Событие не добавлено в ленту!");
            } else {
                log.info("Добавлено событие в ленту!");
            }
            friendStorage.deleteFriend(userId, friendId);
            log.info("Юзер успешно удален");
        }
    }

    public List<User> getFriends(int id) {
        if (userStorage.getUsersSqlRowSet(id).next()) {
            return friendStorage.getFriends(id);
        } else {
            throw new EntityNotFoundException("Пользователь был удален из БД");
        }
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        return friendStorage.getCommonFriends(userId, otherId);
    }

    public List<Film> getFilmsRecommendations(int userId) {
        return userStorage.getFilmsRecommendations(userId);
    }

    public void deleteUser(int id) {
        if (userStorage.getUsersSqlRowSet(id).next()) {
            userStorage.deleteUser(id);
            log.info("Юзер с id " + id + " удален.");
        } else {
            throw new EntityNotFoundException("Юзер с id " + id + " не найден.");
        }
    }

    boolean checkOnContainsUser(int userId) {
        for (User user : userStorage.getUsers()) {
            if (user.getId() == userId) return true;
        }
        return false;
    }

    public boolean containsUserInFriendList(Integer userId, Integer friendId) {
        for (User user : getFriends(userId)) {
            if (user.getId().equals(friendId)) return true;
        }
        return false;
    }
}
