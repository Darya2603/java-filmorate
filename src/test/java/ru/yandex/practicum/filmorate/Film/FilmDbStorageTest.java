package ru.yandex.practicum.filmorate.Film;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.db.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.mapper.FilmMapper;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FilmDbStorageTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private FilmDbStorage filmDbStorage;

    private Film film;

    @BeforeEach
    public void setUp() {
        film = new Film();
        film.setName("Test Film");
        film.setDescription("A test film description.");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(120);
        film.setMpa(new Mpa(1));
    }

    @Test
    public void testGetFilmById() {
        when(jdbcTemplate.queryForObject(anyString(), any(FilmMapper.class), anyInt())).thenReturn(film);

        Film resultFilm = filmDbStorage.getFilmById(1);
        assertNotNull(resultFilm);
        assertEquals(film.getName(), resultFilm.getName());
    }

    @Test
    public void testGetAllFilms() {
        when(jdbcTemplate.query(anyString(), any(FilmMapper.class))).thenReturn(Arrays.asList(film));

        List<Film> films = filmDbStorage.getAllFilms();
        assertFalse(films.isEmpty());
        assertEquals(1, films.size());
        assertEquals(film.getName(), films.get(0).getName());
    }
}