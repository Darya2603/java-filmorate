package ru.yandex.practicum.filmorate.controller;


import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.*;

import ru.yandex.practicum.filmorate.exception.DataAlreadyExistException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final HashMap<Integer, User> users = new HashMap<>();
    private final Set<String> emailUniqSet = new HashSet<>(); // Набор уникальных электронных адресов
    private int counter = 0;

    @PostMapping
    public ResponseEntity<User> addUser(@Valid @RequestBody User user) {
        String email = user.getEmail();
        if (emailUniqSet.contains(email)) {
            throw new DataAlreadyExistException("Email: " + email + " уже существует");
        }

        if (user.getName() == null || user.getName().trim().isEmpty()) {
            user.setName(user.getLogin());
        }

        user.setId(++counter);
        users.put(user.getId(), user);
        emailUniqSet.add(email);

        log.info("Добавлен пользователь: {}", user);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<User> updateUser(@Valid @RequestBody User updatedUser) {
        if (updatedUser.getId() <= 0) {
            log.warn("ID пользователя должен быть положительным");
            return ResponseEntity.badRequest().build();
        }
        if (!users.containsKey(updatedUser.getId())) {
            throw new UserNotFoundException("Пользователь с id " + updatedUser.getId() + " не найден");
        }
        for (User user : users.values()) {
            if (user.getId() != updatedUser.getId()) {
                if (user.getLogin().equals(updatedUser.getLogin())) {
                    log.warn("Логин {} уже используется", updatedUser.getLogin());
                    return ResponseEntity.status(HttpStatus.CONFLICT).build();
                }
                if (user.getEmail().equals(updatedUser.getEmail())) {
                    log.warn("Электронная почта {} уже используется", updatedUser.getEmail());
                    return ResponseEntity.status(HttpStatus.CONFLICT).build();
                }
            }
        }
        users.put(updatedUser.getId(), updatedUser);
        log.info("Обновлен пользователь: {}", updatedUser);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }
}
