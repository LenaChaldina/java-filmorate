package ru.yandex.practicum.filmorate.сontroller;
//создание пользователя;
//обновление пользователя;
//получение списка всех пользователей.

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public User addUser(@Valid @RequestBody User user) {
        return userService.addUser(user);
    }

    @PutMapping
    public User putUser(@Valid @RequestBody User user) {
        return userService.putUser(user);
    }

    @GetMapping
    public List<User> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable("id") int id) {
        return userService.findUserById(id);
    }

    //PUT /users/{id}/friends/{friendId} — добавление в друзья.
    @PutMapping("/{id}/friends/{friendId}")
    public void putFriend(@PathVariable("id") int userId, @PathVariable("friendId") int friendId) {
        userService.addFriend(userId, friendId);
    }

    //DELETE /users/{id}/friends/{friendId} — удаление из друзей.
    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable("id") int userId, @PathVariable("friendId") int friendId) {
        userService.deleteFriend(userId, friendId);
    }

    //GET /users/{id}/friends — возвращаем список пользователей, являющихся его друзьями.
    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable("id") int id) {
        return userService.getFriends(id);
    }

    //GET /users/{id}/friends/common/{otherId} — список друзей, общих с другим пользователем.
    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable("id") int userId, @PathVariable("otherId") int otherId) {
        return userService.getCommonFriends(userId, otherId);
    }
}
