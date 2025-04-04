package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NotFoundException extends IllegalArgumentException {

    public NotFoundException(final String message) {
        super(message);
        log.error(message);
    }
}
