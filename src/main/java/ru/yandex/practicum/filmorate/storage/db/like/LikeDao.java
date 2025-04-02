package ru.yandex.practicum.filmorate.storage.db.like;

import ru.yandex.practicum.filmorate.model.Film;
import java.util.*;

public interface LikeDao {

    void like(int filmId, int userId);

    void dislike(int filmId, int userId);

    int countLikes(int filmId);

    boolean isLiked(int filmId, int userId);

    List<Film> getTopFilms(int count);

    void removeLike(Integer filmId, Integer userId);
}
