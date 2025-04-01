package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ErrorResponse;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.LikeNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import java.util.stream.Collectors;

import java.util.List;
import java.util.*;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;
    private final FilmStorage filmStorage;

    @Autowired
    public FilmController(FilmService filmService, FilmStorage filmStorage) {
        this.filmService = filmService;
        this.filmStorage = filmStorage;
    }

    @PostMapping
    public ResponseEntity<Film> addFilm(@Valid @RequestBody Film film) {
        Film addFilm = filmStorage.addFilm(film);
        return new ResponseEntity<>(addFilm, HttpStatus.CREATED);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<Film>> addFilms(@Valid @RequestBody List<Film> films) {
        List<Film> addFilms = films.stream()
                .map(filmStorage::addFilm)
                .collect(Collectors.toList());
        return new ResponseEntity<>(addFilms, HttpStatus.CREATED);
    }

    @DeleteMapping("/{filmId}")
    public ResponseEntity<Void> deleteFilm(@PathVariable int filmId) {
        try {
            filmStorage.deleteFilm(filmId);
            return ResponseEntity.noContent().build();
        } catch (FilmNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Ошибка при удалении фильма с ID " + filmId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping
    public ResponseEntity<Film> updateFilm(@Valid @RequestBody Film updatedFilm) {
        Film film = filmStorage.updateFilm(updatedFilm);
        return ResponseEntity.ok(film);

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film addFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос на создание фильма: {}", film);
        return filmService.addFilm(film);
    }

    @DeleteMapping("/{filmId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFilm(@PathVariable int filmId) {
        log.info("Получен запрос на удаление фильма с ID: {}", filmId);
        filmService.deleteFilm(filmId);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film updatedFilm) {
        log.info("Получен запрос на обновление фильма: {}", updatedFilm);
        return filmService.updateFilm(updatedFilm);

    }

    @GetMapping
    public List<Film> getFilms() {
        return filmStorage.getAllFilms();
    }

    @GetMapping("/{filmId}")
    public ResponseEntity<?> getFilmById(@PathVariable("filmId") int filmId) {
        Film film = filmStorage.getFilmById(filmId);
        if (film != null) {
            return ResponseEntity.ok(film);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Фильм не найден"));
        }
    }

    @PutMapping("/{filmId}/like/{userId}")
    public ResponseEntity<Map<String, String>> addLike(@PathVariable("filmId") Integer filmId,
                                                       @PathVariable("userId") Integer userId) {
        try {
            filmService.addLike(filmId, userId);
            return ResponseEntity.noContent().build();
        } catch (FilmNotFoundException e) {
            log.warn("Фильм не найден: {}", e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("error", "Фильм не найден");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (UserNotFoundException e) {
            log.warn("Пользователь не найден: {}", e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("error", "Пользователь не найден");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            log.error("Ошибка при добавлении лайка: ", e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "Произошла ошибка при добавлении лайка");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public ResponseEntity<?> removeLike(@PathVariable("filmId") Integer filmId,
                                        @PathVariable("userId") Integer userId) {
        try {
            filmService.removeLike(filmId, userId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (FilmNotFoundException | UserNotFoundException | LikeNotFoundException e) {
            log.warn(e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            log.error("Ошибка при удалении лайка: {}", e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("error", "Внутренняя ошибка сервера");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/popular")
    public ResponseEntity<?> getPopularFilms(@RequestParam(name = "count", defaultValue = "10") int count) {
        try {
            List<Film> topFilms = filmService.getTopFilms(count);
            if (topFilms.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Нет популярных фильмов."));
            }
            return ResponseEntity.ok(topFilms);
        } catch (Exception e) {
            log.error("Ошибка при получении популярных фильмов", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Внутренняя ошибка сервера"));
        }
        log.info("Получен запрос на получение списка фильмов");
        return filmService.getAllFilms();
    }

    @GetMapping("/{filmId}")
    public ResponseEntity<Film> getFilmById(@PathVariable("filmId") int filmId) {
        log.info("Получен запрос на получение фильма с ID: {}", filmId);
        Film film = filmService.getFilmById(filmId);
        return ResponseEntity.ok(film);
    }

    @PutMapping("/{filmId}/like/{userId}")
    public void addLike(@PathVariable("filmId") Integer filmId,
                        @PathVariable("userId") Integer userId) {
        log.info("Получен запрос на добавление лайка фильму с ID: {} от пользователя с ID: {}", filmId, userId);
        filmService.addLike(filmId, userId);
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public void removeLike(@PathVariable("filmId") Integer filmId,
                           @PathVariable("userId") Integer userId) {
        log.info("Получен запрос на удаление лайка у фильма с ID: {} от пользователя с ID: {}", filmId, userId);
        filmService.removeLike(filmId, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(name = "count", defaultValue = "10") int count) {
        log.info("Получен запрос на получение популярных фильмов, количество: {}", count);
        return filmService.getTopFilms(count);
    }
}



