package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private int id;
    @NotBlank(message = "Электронная почта не может быть пустой")
    @Email(message = "Электронная почта должна содержать символ @")
    private String email;

    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "^[^\\s]+$", message = "Логин не должен содержать пробелы")
    private String login;

    private String name;

    @Past(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;

    private Set<Integer> friends = new HashSet<>();
    private Set<Friendship> friendRequests = new HashSet<>();
    private int friendCount;

    public void updateFriendCount() {
        this.friendCount = friends.size();
    }

    public void sendFriendRequest(User user) {
        Friendship request = new Friendship(this.id, user.getId());
        user.getFriendRequests().add(request);
    }

    public void acceptFriendRequest(Friendship request) {
        request.confirmFriendship();
        this.friends.add(request.getRequesterId()); // Добавляем в друзья
        this.friendRequests.remove(request); // Удаляем заявку
        updateFriendCount(); // Обновляем количество друзей
    }
}