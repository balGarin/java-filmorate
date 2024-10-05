package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {

    User addUser(User newUser);

    User updateUser(User newUser);

    List<User> getAllUsers();

    User getById(Integer id);

    void addFriend(Integer id, Integer friendId);

    void deleteFriend(Integer id, Integer friendId);

    List<User> getListOfFriends(Integer id);

    List<User> getListOfCommonFriends(Integer id, Integer otherId);

}
