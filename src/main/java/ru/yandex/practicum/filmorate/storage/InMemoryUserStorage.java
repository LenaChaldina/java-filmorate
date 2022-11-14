package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.InvalidUpdateException;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class InMemoryUserStorage implements UserStorage {
    private Map<Integer, User> users = new HashMap<>();
    private int id = 1;

    public User addUser(User user) {
        //имя для отображения может быть пустым — в таком случае будет использован логин;
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        user.setId(id++);
        users.put(user.getId(), user);
        log.info("Юзер успешно добавлен");
        return user;
    }

    public User putUser(User user) {
        //имя для отображения может быть пустым — в таком случае будет использован логин;
        if (users.containsKey(user.getId())) {
            if (user.getName() == null || user.getName().isBlank()) {
                user.setName(user.getLogin());
            }
            users.put(user.getId(), user);
            log.info("Юзер успешно обновлен");
        } else {
            throw new EntityNotFoundException("Taкого юзера нет в списке");
        }
        return user;
    }

    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    public User findUserById(int id) {
        if (users.containsKey(id)) {
            return users.get(id);
        } else {
            throw new EntityNotFoundException("Такого юзера нет");
        }
    }

}
