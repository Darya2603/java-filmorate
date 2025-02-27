package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmRepository {
    Film addFilm(Film film);
    Film getFilmById(int id);
    List<Film> getAllFilms();
    void deleteFilm(int id);
}
