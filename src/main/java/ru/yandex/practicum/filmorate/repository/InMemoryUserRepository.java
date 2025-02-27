package ru.yandex.practicum.filmorate.repository;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryUserRepository implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private long currentId = 0;

    @Override
    public User addUser(User user) {
        user.setId((int) ++currentId);
        users.put((long) user.getId(), user);
        return user;
    }

    @Override
    public User getUserById(long id) {
        return users.get(id);
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void deleteUser(long id) {
        users.remove(id);
    }

    @Override
    public boolean existsById(long id) {
        return users.containsKey(id); // Проверка на наличие пользователя
    }
}
