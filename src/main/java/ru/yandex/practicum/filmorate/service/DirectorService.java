package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.DirectorStorage;
import ru.yandex.practicum.filmorate.dao.impl.DirectorDbStorage;
import ru.yandex.practicum.filmorate.exceptions.RequestError;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.Objects;

@Service
@Slf4j
public class DirectorService implements DirectorStorage {

    Integer id = 1;
    final DirectorDbStorage directorDbStorage;

    public DirectorService(@Qualifier("directorDbStorage") DirectorDbStorage directorDbStorage) {
        this.directorDbStorage = directorDbStorage;
    }

    @Override
    public Director createDirector(Director director) {
        director.setId(id);
        id++;
        log.info("Добавлен новый режиссер {}", director.getName());
        return directorDbStorage.createDirector(director);
    }

    @Override
    public Director updateDirector(Director director) {
        Director updateDirector = directorDbStorage.updateDirector(director);
        if (updateDirector != null) {
            log.info("Информация о режиссере обновлена {}", director.getName());
            return updateDirector;
        }
        log.warn("Ошибка. Режиссер не найден в списке");
        throw new RequestError(HttpStatus.NOT_FOUND
                , "Ошибка. Невозможно обновить несуществующий фильм");
    }

    @Override
    public Collection<Director> getAllDirectors() {
        return directorDbStorage.getAllDirectors();
    }

    @Override
    public Director getDirectorById(Integer id) {
        if (checkContainsDirectorInList(id)) {
            log.info("Получен запрос информации о режиссере {}", id);
            return directorDbStorage.getDirectorById(id);
        }
        log.warn("Получен запрос на не существующего в списке режиссера");
        throw new RequestError(HttpStatus.NOT_FOUND, "Режиссер не найден");
    }

    @Override
    public void deleteDirectorById(Integer id) {
        log.info("Режиссер с id = {} удален из списка", id);
        directorDbStorage.deleteDirectorById(id);
    }

    private boolean checkContainsDirectorInList(Integer id) {
        for (Director director : directorDbStorage.getAllDirectors()) {
            if (Objects.equals(director.getId(), id)) return true;
        }
        return false;
    }
}