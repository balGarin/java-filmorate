package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.IncorrectDataException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;

    public UserService(@Qualifier("DBUsers") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User getById(Integer id) {
        return userStorage.getById(id);
    }

    public void addFriend(Integer id, Integer friendId) {
       userStorage.addFriend(id, friendId);
    }

    public void deleteFriend(Integer id, Integer friendId) {
        userStorage.deleteFriend(id, friendId);
    }

    public List<User> getListOfFriends(Integer id) {
      return  userStorage.getListOfFriends(id);
    }


    public List<User> getListOfCommonFriends(Integer id, Integer otherId) {
        return userStorage.getListOfCommonFriends(id, otherId);
    }







}
