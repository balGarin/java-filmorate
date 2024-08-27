package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {

    public User addUser(User newUser);

    public User updateUser(User newUser);

    public List<User> getAllUsers();

    public User getById(Integer id);
}
