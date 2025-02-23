package ru.yandex.practicum.filmorate.Film;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilmValidationTest {
    private final Validator validator;

    public FilmValidationTest() {
        // Используем try-with-resources для автоматического закрытия ValidatorFactory
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    public void testValidFilm() {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("This is a valid film description.");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testBlankName() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Some description");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
        assertEquals("Название не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    public void testDescriptionTooLong() {
        Film film = new Film();
        film.setName("Long Description Film");
        film.setDescription("A".repeat(201)); // 201 символ
        film.setReleaseDate(LocalDate.now());
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
        // Исправляем сообщение об ошибке на то, что вы получаете от валидатора
        assertEquals("Максимальная длина описания — 200 символов", violations.iterator().next().getMessage());
    }

    @Test
    public void testNegativeDuration() {
        Film film = new Film();
        film.setName("Film with Negative Duration");
        film.setDescription("Some description");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(-1);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
        assertEquals("Продолжительность фильма должна быть положительным числом", violations.iterator().next().getMessage());
    }

    @Test
    public void testReleaseDateInPast() {
        Film film = new Film();
        film.setName("Film with Past Release Date");
        film.setDescription("Some description");
        film.setReleaseDate(LocalDate.of(1890, 1, 1)); // до 28 декабря 1895
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
        // Исправляем сообщение об ошибке на то, что вы получаете от валидатора
        assertEquals("Дата релиза не может быть раньше 28 декабря 1895 года", violations.iterator().next().getMessage());
    }
}
