package ru.yandex.practicum.filmorate.exceptions;

public class EntityDuplicateException extends RuntimeException {
    public EntityDuplicateException(String message) {
        super(message);
    }
}