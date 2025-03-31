package ru.yandex.practicum.filmorate.storage.db.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import java.util.*;

import java.util.List;

public interface FilmStorage {
    Film addFilm(Film film);

    void deleteFilm(int filmId);

    Film updateFilm(Film updatedFilm);

    List<Film> getAllFilms();

    Film getFilmById(int filmId);

    Set<Genre> getGenres(int filmId);

    List<Film> getAllFilmsWithGenres();

    boolean isContains(int id);
}
