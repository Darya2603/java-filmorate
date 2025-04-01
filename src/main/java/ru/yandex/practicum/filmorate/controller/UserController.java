package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserStorage userStorage;
    private final UserService userService;

    @Autowired
    public UserController(UserStorage userStorage, UserService userService) {
        this.userStorage = userStorage;
      
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<User> addUser(@Valid @RequestBody User user) {
        User addUser = userStorage.addUser(user);
        return new ResponseEntity<>(addUser, HttpStatus.CREATED);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<User>> addUsers(@Valid @RequestBody List<User> users) {
        List<User> addUsers = users.stream()
                .map(userStorage::addUser)
                .collect(Collectors.toList());
        return new ResponseEntity<>(addUsers, HttpStatus.CREATED);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable int userId) {
        try {
            userStorage.deleteUser(userId);
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Ошибка при удалении пользователя с ID " + userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping
    public ResponseEntity<User> updateUser(@Valid @RequestBody User updatedUser) {
        User user = userStorage.updateUser(updatedUser);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public List<User> getUsers() {
        return userStorage.getAllUsers();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable int userId) {
        try {
            User user = userStorage.getUserById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Ошибка при получении пользователя с ID " + userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/{userId}/friends/{friendId}")
    public ResponseEntity<Map<String, String>> addFriend(@PathVariable("userId") int userId,
                                                         @PathVariable("friendId") int friendId) {
        try {
            userService.addFriend(userId, friendId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Друг успешно добавлен");
            return ResponseEntity.ok(response);
        } catch (UserNotFoundException e) {
            log.warn("Пользователь не найден: {}", e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("error", "Пользователь с ID " + friendId + " не найден");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            log.error("Ошибка при добавлении друга: {}", e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("error", "Произошла ошибка при добавлении друга");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    public ResponseEntity<Map<String, String>> removeFriend(@PathVariable("userId") int userId,
                                                            @PathVariable("friendId") int friendId) {
        try {
            userService.removeFriend(userId, friendId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Друг успешно удален");

            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException e) {
            log.warn("Пользователь не найден: {}", e.getMessage());

            Map<String, String> response = new HashMap<>();
            response.put("error", "Пользователь с ID " + userId + " или друг с ID " + friendId + " не найден");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("Ошибка: " + e.getMessage());

            Map<String, String> response = new HashMap<>();
            response.put("error", "Некорректный запрос");

            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("Ошибка при удалении друга: ", e);

            Map<String, String> response = new HashMap<>();
            response.put("error", "Произошла ошибка при удалении друга");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{userId}/friends")
    public ResponseEntity<?> getFriends(@PathVariable("userId") int userId) {
        try {
            User user = userStorage.getUserById(userId);
            if (user == null) {
                log.warn("Пользователь не найден: userId={}", userId);
                Map<String, String> response = new HashMap<>();
                response.put("error", "Пользователь с ID " + userId + " не найден");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            List<User> friends = userStorage.getFriendsByUserId(userId);
            return ResponseEntity.ok(friends);
        } catch (Exception e) {
            log.error("Ошибка при получении друзей пользователя с ID " + userId, e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "Произошла ошибка при получении друзей");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{userId1}/friends/common/{userId2}")
    public ResponseEntity<?> getCommonFriends(@PathVariable("userId1") Integer userId1,
                                              @PathVariable("userId2") Integer userId2) {
        try {
            List<User> commonFriends = userService.getCommonFriends(userId1, userId2);
            return ResponseEntity.ok(commonFriends);
        } catch (UserNotFoundException e) {
            log.warn("Пользователь не найден: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Один из пользователей не найден");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            log.error("Ошибка при получении общих друзей: ", e);
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Произошла ошибка при получении общих друзей");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    @ResponseStatus(HttpStatus.CREATED)
    public User addUser(@Valid @RequestBody User user) {
        log.info("Получен запрос на создание пользователя: {}", user);
        return userService.addUser(user);
    }

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public List<User> addUsers(@Valid @RequestBody List<User> users) {
        log.info("Получен запрос на создание группы пользователей: {}", users);

        List<User> addedUsers = userService.addUsers(users);

        log.info("Успешно добавлены пользователи: {}", addedUsers);
        return addedUsers;
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Integer userId) {
        log.info("Получен запрос на удаление пользователя с ID: {}", userId);
        userService.deleteUser(userId);
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User updatedUser) {
        log.info("Получен запрос на обновление пользователя: {}", updatedUser);
        return userService.updateUser(updatedUser);
    }

    @GetMapping
    public List<User> getAllUsers() {
        log.info("Получен запрос на получение всех пользователей");
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}")
    public Optional<User> getUserById(@PathVariable Integer userId) {
        log.info("Получен запрос на получение пользователя с ID: {}", userId);
        return userService.getUserById(userId);
    }

    @PutMapping("/{userId}/friends/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addFriend(@PathVariable("userId") Integer userId,
                          @PathVariable("friendId") Integer friendId) {
        log.info("Получен запрос на добавление друга: пользователь ID={}, друг ID={}", userId, friendId);
        userService.addFriend(userId, friendId);
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // Убедитесь, что статус 204 возвращается
    public void removeFriend(@PathVariable("userId") Integer userId,
                             @PathVariable("friendId") Integer friendId) {
        log.info("Получен запрос на удаление друга: пользователь ID={}, друг ID={}", userId, friendId);
        userService.removeFriend(userId, friendId);
    }

    @GetMapping("/{userId}/friends")
    public List<User> getFriends(@PathVariable("userId") int userId) {
        log.info("Получен запрос на получение друзей пользователя с ID: {}", userId);
        return userService.getFriends(userId);
    }

    @GetMapping("/{userId1}/friends/common/{userId2}")
    public List<User> getCommonFriends(@PathVariable("userId1") Integer userId1,
                                       @PathVariable("userId2") Integer userId2) {
        log.info("Получен запрос на получение общих друзей: пользователь1 ID={}, пользователь2 ID={}", userId1, userId2);
        return userService.getCommonFriends(userId1, userId2);
    }
}
