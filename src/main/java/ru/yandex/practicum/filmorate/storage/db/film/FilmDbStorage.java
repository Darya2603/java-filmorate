package ru.yandex.practicum.filmorate.storage.db.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.EntityNotFoundException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.storage.mapper.GenreMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.sql.Date;

@Slf4j
@Component("FilmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film addFilm(Film film) {
        log.debug("createFilm({})", film);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        film.setId(keyHolder.getKey().intValue());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            addGenres(film.getId(), film.getGenres());
        }

        log.trace("The movie {} was added to the database", film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        log.debug("updateFilm({}).", film);

        if (film.getId() == null || film.getId() <= 0 || !isContains(film.getId())) {
            throw new NotFoundException("Attempt to update non-existing movie with id " + film.getId());
        }

        jdbcTemplate.update(
                "UPDATE films SET name=?, description=?, release_date=?, duration=?, mpa_id=? WHERE film_id=?",
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        Film thisFilm = getFilmById(film.getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Genre> uniqueGenres = new HashSet<>(film.getGenres());
            updateGenres(thisFilm.getId(), uniqueGenres);
        }

        log.trace("The movie {} was updated in the database", thisFilm);
        return thisFilm;
    }

    @Override
    public Film getFilmById(int id) {
        log.debug("getFilmById({})", id);
        try {
            Film thisFilm = jdbcTemplate.queryForObject(
                    "SELECT film_id, name, description, release_date, duration, mpa_id FROM films WHERE film_id=?",
                    new FilmMapper(), id);
            log.trace("The movie {} was returned", thisFilm);
            return thisFilm;
        } catch (EmptyResultDataAccessException e) {
            log.warn("No film found with id {}", id);
            throw new NotFoundException("Film with id " + id + " not found");
        }
    }

    @Override
    public List<Film> getAllFilms() {
        log.debug("getAllFilms()");
        List<Film> films = jdbcTemplate.query(
                "SELECT film_id, name, description, release_date, duration, mpa_id FROM films",
                new FilmMapper());
        log.trace("Returned {} films from the database", films.size());
        return films;
    }

    private void addGenres(int filmId, Set<Genre> genres) {
        log.debug("addGenres({}, {})", filmId, genres);

        Set<Genre> uniqueGenres = new HashSet<>();

        for (Genre genre : genres) {
            if (uniqueGenres.add(genre)) {
                jdbcTemplate.update("INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)", filmId, genre.getId());
                log.trace("Genre {} was added to movie {}", genre.getName(), filmId);
            } else {
                log.trace("Duplicate genre {} found in input and will not be added", genre.getName());
            }
        }
    }

    private void updateGenres(int filmId, Set<Genre> genres) {
        log.debug("updateGenres({}, {})", filmId, genres);
        deleteGenres(filmId);
        addGenres(filmId, genres);
    }

    @Override
    public Set<Genre> getGenres(int filmId) {
        log.debug("getGenres({})", filmId);
        Set<Genre> genres = new HashSet<>(jdbcTemplate.query(
                "SELECT f.genre_id, g.genre_type FROM film_genre AS f " +
                        "LEFT OUTER JOIN genre AS g ON f.genre_id = g.genre_id WHERE f.film_id=? ORDER BY g.genre_id",
                new GenreMapper(), filmId));
        log.trace("Genres for the movie with id {} were returned", filmId);
        return genres;
    }

    @Override
    public List<Film> getAllFilmsWithGenres() {
        log.debug("getAllFilmsWithGenres()");
        String sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, " +
                "g.genre_id, g.genre_type FROM films AS f " +
                "LEFT JOIN film_genre AS fg ON f.film_id = fg.film_id " +
                "LEFT JOIN genre AS g ON fg.genre_id = g.genre_id " +
                "ORDER BY f.film_id";

        List<Film> films = jdbcTemplate.query(sql, new FilmMapper());
        log.trace("Returned {} films with genres from the database", films.size());
        return films;
    }

    private void deleteGenres(int filmId) {
        log.debug("deleteGenres({})", filmId);
        jdbcTemplate.update("DELETE FROM film_genre WHERE film_id=?", filmId);
        log.trace("All genres were removed for a movie with id {}", filmId);
    }

    @Override
    public boolean isContains(int id) {
        log.debug("isContains({})", id);
        try {
            getFilmById(id);
            log.trace("The movie with id {} was found", id);
            return true;
        } catch (EmptyResultDataAccessException exception) {
            log.trace("No information has been found for id {}", id);
            return false;
        }
    }

    @Override
    public void deleteFilm(int id) {
        log.debug("deleteFilm({})", id);
        int rowsAffected = jdbcTemplate.update("DELETE FROM films WHERE film_id = ?", id);

        if (rowsAffected > 0) {
            log.trace("The movie with id {} was deleted from the database", id);
        } else {
            log.warn("Attempted to delete a movie with id {} that does not exist", id);
            throw new EntityNotFoundException("Film with id " + id + " not found");
        }
    }
}