package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.impl.DirectorDbStorage;
import ru.yandex.practicum.filmorate.exceptions.RequestError;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.Objects;

@Service
@Slf4j
public class DirectorService {

    private Integer id = 1;
    private final DirectorDbStorage directorDbStorage;

    public DirectorService(@Qualifier("directorDbStorage") DirectorDbStorage directorDbStorage) {
        this.directorDbStorage = directorDbStorage;
    }


    public Director createDirector(Director director) {
        director.setId(id);
        id++;
        log.info("Добавлен новый режиссер {}", director.getName());
        return directorDbStorage.createDirector(director);
    }

    public Director updateDirector(Director director) {
        Director updateDirector = directorDbStorage.updateDirector(director);
        if (updateDirector == null) {
            log.warn("Ошибка. Режиссер не найден в списке");
            throw new RequestError(HttpStatus.NOT_FOUND
                    , "Ошибка. Невозможно обновить несуществующий фильм");
        }
        log.info("Информация о режиссере обновлена {}", director.getName());
        return updateDirector;
    }

    public Collection<Director> getAllDirectors() {
        return directorDbStorage.getAllDirectors();
    }

    public Director getDirectorById(Integer id) {
        if (checkContainsDirectorInList(id)) {
            log.info("Получен запрос информации о режиссере {}", id);
            return directorDbStorage.getDirectorById(id);
        }
        log.warn("Получен запрос на не существующего в списке режиссера");
        throw new RequestError(HttpStatus.NOT_FOUND, "Режиссер не найден");
    }

    public void deleteDirectorById(Integer id) {
        log.info("Режиссер с id = {} удален из списка", id);
        directorDbStorage.deleteDirectorById(id);
    }

    private boolean checkContainsDirectorInList(Integer id) {
        for (Director director : directorDbStorage.getAllDirectors()) {
            if (Objects.equals(director.getId(), id) && director != null) return true;
        }
        return false;
    }
}