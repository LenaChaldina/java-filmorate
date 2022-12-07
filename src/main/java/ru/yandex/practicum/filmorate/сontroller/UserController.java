package ru.yandex.practicum.filmorate.сontroller;
//создание пользователя;
//обновление пользователя;
//получение списка всех пользователей.

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FeedService;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private UserService userService;
    private final FeedService feedService;

    @Autowired
    public UserController(UserService userService, FeedService feedService) {
        this.userService = userService;
        this.feedService = feedService;
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
    //`GET /users/{id}/recommendations`
    //Возвращает лист рекомендуемых фильмов для просмотра
    @GetMapping("/{id}/recommendations")
    public List<Film> getFilmsRecommendations(@PathVariable("id") int userId) {
        return userService.getFilmsRecommendations(userId);
    }

    @GetMapping("/{id}/feed")
    public List<Feed> getFeedByUserId(@PathVariable("id") int userId) {
        return feedService.getFeedByUserId(userId);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable int id) {
        userService.deleteUser(id);
    }
}
