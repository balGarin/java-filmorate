package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {

    User addUser(User newUser);

    User updateUser(User newUser);

    List<User> getAllUsers();

    User getById(Integer id);
}
