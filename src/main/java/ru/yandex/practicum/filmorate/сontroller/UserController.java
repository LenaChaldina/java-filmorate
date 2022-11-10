package ru.yandex.practicum.filmorate.сontroller;
//создание пользователя;
//обновление пользователя;
//получение списка всех пользователей.

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.InvalidUpdateException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private Map<Integer, User> users = new HashMap<>();
    private int id = 1;

    @PostMapping
    public User addUser(@Valid @RequestBody User user) {
        //имя для отображения может быть пустым — в таком случае будет использован логин;
        if(user.getName() == null) {
            user.setName(user.getLogin());
        }
        user.setId(id++);
        users.put(user.getId(), user);
        log.info("Юзер успешно добавлен");
        return user;
    }

    @PutMapping
    public User putUser(@Valid @RequestBody User user) {
        //имя для отображения может быть пустым — в таком случае будет использован логин;
        if(users.containsKey(user.getId())) {
            if(user.getName() == null) {
                user.setName(user.getLogin());
            }
            users.put(user.getId(), user);
            log.info("Юзер успешно обновлен");
        }else {
            throw new InvalidUpdateException("Taкого юзера нет в списке");
        }
        return user;
    }

    @GetMapping
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }
}
