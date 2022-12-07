package ru.yandex.practicum.filmorate.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class RequestErrorForReview extends ResponseStatusException {

    public RequestErrorForReview(HttpStatus status, String message) {
        super(status, message);
    }
}