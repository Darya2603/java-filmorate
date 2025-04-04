package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.db.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.db.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.db.genre.GenreDao;
import ru.yandex.practicum.filmorate.storage.db.like.LikeDao;
import ru.yandex.practicum.filmorate.storage.db.mpa.MpaDao;
import ru.yandex.practicum.filmorate.storage.db.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.db.user.UserStorage;

import java.util.stream.Collectors;
import java.util.List;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreDao genreDao;
    private final MpaDao mpaDao;
    private final LikeDao likeDao;

    @Autowired
    public FilmService(@Qualifier("FilmDbStorage") FilmDbStorage filmStorage,
                       @Qualifier("UserDbStorage") UserDbStorage userStorage,
                       GenreDao genreDao,
                       MpaDao mpaDao,
                       LikeDao likeDao) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreDao = genreDao;
        this.mpaDao = mpaDao;
        this.likeDao = likeDao;
    }

    public Film addFilm(Film film) {
        log.debug("createFilm({})", film);

        int mpaId = film.getMpa().getId();
        if (!mpaDao.isContains(mpaId)) {
            log.warn("MPA with ID {} wasn't found", mpaId);
            throw new NotFoundException("MPA with ID " + mpaId + " wasn't found");
        }

        log.debug("MPA ID is valid: {}", mpaId);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            List<Integer> existingGenreIds = genreDao.getAllGenreIds();

            for (Genre genre : film.getGenres()) {
                if (!existingGenreIds.contains(genre.getId())) {
                    log.warn("Genre with ID {} wasn't found", genre.getId());
                    throw new NotFoundException("Genre with ID " + genre.getId() + " wasn't found");
                }
            }
        }

        Film addedFilm = filmStorage.addFilm(film);
        log.trace("The movie {} was added to the database", addedFilm);
        return addedFilm;
    }

    public void deleteFilm(int filmId) {
        filmStorage.deleteFilm(filmId);
    }

    public Film updateFilm(Film updatedFilm) {
        return filmStorage.updateFilm(updatedFilm);
    }

    public List<Film> getAllFilms() {
        List<Film> films = filmStorage.getAllFilmsWithGenres();
        log.trace("Returned {} films from the database", films.size());
        return films;
    }

    public Film getFilmById(int filmId) {
        if (!filmStorage.isContains(filmId)) {
            throw new NotFoundException("Unable to find a movie with id " + filmId);
        }
        Film film = filmStorage.getFilmById(filmId);
        film.setGenres(filmStorage.getGenres(filmId));
        film.setMpa(mpaDao.getMpaById(film.getMpa().getId()));
        film.setLikesCount(likeDao.countLikes(film.getId()));
        return film;
    }

    public void addLike(Integer filmId, Integer userId) {
        if (filmId == null || userId == null) {
            throw new IllegalArgumentException("Фильм ID и пользователь ID не могут быть null");
        }

        Film film = getFilmById(filmId);
        if (film == null) {
            throw new NotFoundException("Фильм с ID " + filmId + " не найден");
        }

        if (!userStorage.userExists(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }

        if (!likeDao.isLiked(filmId, userId)) {
            likeDao.like(filmId, userId);
            log.info("Пользователь с ID {} добавил лайк к фильму с ID {}", userId, filmId);
        } else {
            log.info("Пользователь с ID {} уже лайкал фильм с ID {}", userId, filmId);
        }
    }

    public void removeLike(Integer filmId, Integer userId) {
        if (filmId == null || userId == null) {
            throw new IllegalArgumentException("Фильм ID и пользователь ID не могут быть null");
        }

        Film film = getFilmById(filmId);
        if (film == null) {
            throw new NotFoundException("Фильм с ID " + filmId + " не найден");
        }

        if (!userStorage.userExists(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }

        log.info("Текущие лайки для фильма {}: {}", filmId, film.getLikes());        if (likeDao.isLiked(filmId, userId)) {
            likeDao.dislike(filmId, userId);
            log.info("Пользователь с ID {} убрал лайк к фильму с ID {}", userId, filmId);
        } else {
            throw new IllegalArgumentException("Пользователь не имеет лайка к фильму с ID " + filmId);
        }
    }

    public List<Film> getTopFilms(int count) {
        log.debug("getPopularMovies({})", count);
        List<Film> popularMovies = getAllFilms()
                .stream()
                .sorted(this::compare)
                .limit(count)
                .collect(Collectors.toList());
        log.trace("These are the most popular movies: {}", popularMovies);
        return popularMovies;
    }

    private int compare(Film film, Film otherFilm) {
        return Integer.compare(likeDao.countLikes(otherFilm.getId()), likeDao.countLikes(film.getId()));
    }
}