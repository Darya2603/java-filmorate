package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserRepository {
    User addUser(User user);
    User getUserById(long id);
    List<User> getAllUsers();
    void deleteUser(long id);
    boolean existsById(long id); // Новый метод
}