package ru.yandex.practicum.filmorate.service;

//Создайте UserService, который будет отвечать за такие операции с пользователями,
// как добавление в друзья, удаление из друзей, вывод списка общих друзей.
// Пока пользователям не надо одобрять заявки в друзья — добавляем сразу.
// То есть если Лена стала другом Саши, то это значит, что Саша теперь друг Лены.

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private UserStorage userStorage;
    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
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

    public User findUserById(int id){
        return userStorage.findUserById(id);
    }
    public void addFriend(int userId, int friendId){
        if (userStorage.findUserById(userId) == null) {
            throw new EntityNotFoundException("Такого юзера " + userId + " нет");
        }
        if (userStorage.findUserById(friendId) == null) {
            throw new EntityNotFoundException("Такого друга " + friendId + "нет");
        } else {
            userStorage.findUserById(userId).addFriend(friendId);
            userStorage.findUserById(friendId).addFriend(userId);
        }
    }
    public void deleteFriend(int userId, int friendId){
        if (userStorage.findUserById(userId) == null){
            throw new EntityNotFoundException("Такого юзера "+ userId+" нет");
        }
        if (userStorage.findUserById(friendId) == null){
            throw new EntityNotFoundException("Такого друга " + friendId + "нет");
        } else {
            userStorage.findUserById(userId).removeFriend(friendId);
            userStorage.findUserById(friendId).removeFriend(userId);
        }
    }
    public List<User> getFriends(int id) {
        return userStorage.findUserById(id).getFriends().stream().map(userStorage::findUserById).collect(Collectors.toList());
    }
    public List<User> getCommonFriends(int userId, int otherId) {
        return userStorage.findUserById(userId).getFriends().stream().filter(userStorage.findUserById(otherId).getFriends()::contains).map(userStorage::findUserById).collect(Collectors.toList());
    }
}
