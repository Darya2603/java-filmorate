package ru.yandex.practicum.filmorate.Film;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.db.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.db.genre.GenreDao;
import ru.yandex.practicum.filmorate.storage.db.mpa.MpaDao;
import java.util.Collections;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

public class FilmDbStorageTest {

    @Mock
    private MpaDao mpaDao;

    @Mock
    private GenreDao genreDao;

    @InjectMocks
    private FilmDbStorage filmDbStorage;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addFilmShouldThrowNotFoundExceptionWhenMpaNotFound() {
        Film film = new Film();
        film.setMpa(new Mpa(999));

        when(mpaDao.isContains(999)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class, () -> filmDbStorage.addFilm(film));
        assertEquals("MPA with ID 999 wasn't found", exception.getMessage());
    }

    @Test
    void addFilmShouldThrowNotFoundExceptionWhenGenreNotFound() {
        Film film = new Film();
        film.setMpa(new Mpa(1));
        film.setGenres(Collections.singleton(new Genre(999)));

        when(mpaDao.isContains(1)).thenReturn(true);
        when(genreDao.isContains(999)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class, () -> filmDbStorage.addFilm(film));
        assertEquals("Genre with ID 999 wasn't found", exception.getMessage());
    }
}