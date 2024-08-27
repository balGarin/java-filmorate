package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.IncorrectDataException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    UserStorage userStorage;

    public UserService(InMemoryUserStorage userStorage) {
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
        if (id.equals(friendId)) {
            log.warn("Id пользователя - {} и его друга - {} совпали", id, friendId);
            throw new IncorrectDataException("Нельзя добавить в друзья самого себя");
        }
        User user = userStorage.getById(id);
        User newFriend = userStorage.getById(friendId);
        if (!createFriendship(id, friendId)) {
            log.warn("Пользователь - {} и его друг - {} уже являются друзьями", id, friendId);
            throw new IncorrectDataException("Пользователи уже являются друзьями!");
        }
        log.info("Пользователь номер - {} добавил в друзья пользователя номер - {}", id, friendId);
        createFriendship(friendId, id);

    }

    public void deleteFriend(Integer id, Integer friendId) {
        User user = userStorage.getById(id);
        User friend = userStorage.getById(friendId);
        removeFriend(id, friendId);
        removeFriend(friendId, id);
    }

    public List<User> getListOfFriends(Integer id) {
        Set<Integer> friends = userStorage.getById(id).getFriends();
        List<User> listOfFriends = userStorage.getAllUsers().stream()
                .filter(user -> friends.contains(user.getId()))
                .collect(Collectors.toList());
        log.info("Запрос на получение списка друзей : {}", listOfFriends);
        return listOfFriends;
    }


    public List<User> getListOfCommonFriends(Integer id, Integer otherId) {
        Set<Integer> friends = userStorage.getById(id).getFriends();
        Set<Integer> friendsOtherUser = userStorage.getById(otherId).getFriends();
        List<User> commonFriends = userStorage.getAllUsers().stream()
                .filter(user1 -> friends.contains(user1.getId()))
                .filter(user2 -> friendsOtherUser.contains(user2.getId()))
                .collect(Collectors.toList());
        log.info("Запрос на получение общих друзей для : {} , {}  : {}", id, otherId, commonFriends);
        return commonFriends;
    }


    private void removeFriend(Integer userId, Integer friendId) {
        User user = userStorage.getById(userId);
        Set<Integer> friends = user.getFriends();
        if (friends.remove(friendId)) {
            log.info("Пользователь - {} был удален из друзей у - {}", userId, friendId);
        } else {
            log.debug("Пользователь - {} не был найден в списке друзей у - {}", friendId, userId);
        }
    }

    private boolean createFriendship(Integer userid, Integer friendId) {
        User user = userStorage.getById(userid);
        Set<Integer> friends = user.getFriends();
        return friends.add(friendId);
    }


}
