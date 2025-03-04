package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.ArrayList;
import java.util.List;

import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import java.util.*;


@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final HashMap<Integer, Film> films = new HashMap<>();
    private int currentId = 0;

    @PostMapping
    public ResponseEntity<Film> addFilm(@Valid @RequestBody Film film) {
        int newId = films.size() + 1;
        film.setId(newId);
        currentId++;
        film.setId(currentId);
        if (films.containsKey(film.getId())) {
            log.warn("Фильм с id {} уже существует", film.getId());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        films.put(film.getId(), film);
        log.info("Добавлен фильм: {}", film);
        return new ResponseEntity<>(film, HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<Film> updateFilm(@Valid @RequestBody Film updatedFilm) {
        if (!films.containsKey(updatedFilm.getId())) {
            throw new UserNotFoundException("Пользователь с id " + updatedFilm.getId() + " не найден");
        }
        for (Film film : films.values()) {
            if (film.getId() != updatedFilm.getId() && film.getName().equals(updatedFilm.getName())) {
                log.warn("Название фильма {} уже используется", updatedFilm.getName());
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        }
        films.put(updatedFilm.getId(), updatedFilm);
        log.info("Обновлен фильм: {}", updatedFilm);
        return ResponseEntity.ok(updatedFilm);
    }

    @GetMapping
    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }
}



