package ru.yandex.practicum.filmorate.storage;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.IncorrectDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Repository("InMemoryUsers")
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();


    @Override
    public User addUser(User newUser) {
        log.debug("Валидация успешно пройдена!");
        newUser.setId(generateId());
        newUser.setFriends(new HashSet<>());
        if (newUser.getName() == null) {
            newUser.setName(newUser.getLogin());
            log.debug("Имя юзера не указано и было заменено на {}", newUser.getLogin());

        }
        users.put(newUser.getId(), newUser);
        log.info("Добавлен новый пользователь {}", newUser);
        return newUser;
    }

    @Override
    public User updateUser(User newUser) {
        log.debug("Валидация успешно пройдена!");
        log.info("Новый юзер для обновления {}", newUser.toString());
        if (newUser.getId() == null) {
            log.warn("Ошибка валидации : не указан id");
            throw new IncorrectDataException("Ошибка валидации : Не укажите id");
        }
        if (!users.containsKey(newUser.getId())) {
            log.warn("Ошибка валидации : указан некорректный id");
            throw new NotFoundException("Ошибка валидации : указан некорректный id");

        }
        User user = users.get(newUser.getId());
        user.setLogin(newUser.getLogin());
        if (newUser.getName() != null) {
            user.setName(newUser.getName());
        }
        if (newUser.getEmail() != null) {
            user.setEmail(newUser.getEmail());
        }
        if (newUser.getBirthday() != null) {
            user.setBirthday(newUser.getBirthday());
        }
        log.info("Пользователь № {} успешно обновлен", user);
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        log.debug("Запрос на список всех пользователей : {}", users.values());
        return new ArrayList<>(users.values());
    }


    @Override
    public User getById(Integer id) {
        if (!users.containsKey(id)) {
            log.warn("Введен не верный id - {}", id);
            throw new NotFoundException("Пользователь с таким ID не найден!");
        }
        log.debug("Запрос на пользователя номер - {}", id);
        return users.get(id);
    }

    private int generateId() {
        int id = users.keySet().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
        log.debug("Новый id - {}", id);
        return ++id;
    }

    @Override
    public void addFriend(Integer id, Integer friendId) {
        if (id.equals(friendId)) {
            log.warn("Id пользователя - {} и его друга - {} совпали", id, friendId);
            throw new IncorrectDataException("Нельзя добавить в друзья самого себя");
        }
        User user = getById(id);
        User newFriend = getById(friendId);
        if (!createFriendship(id, friendId)) {
            log.warn("Пользователь - {} и его друг - {} уже являются друзьями", id, friendId);
            throw new IncorrectDataException("Пользователи уже являются друзьями!");
        }
        log.info("Пользователь номер - {} добавил в друзья пользователя номер - {}", id, friendId);
        createFriendship(friendId, id);

    }

    private boolean createFriendship(Integer userid, Integer friendId) {
        User user = getById(userid);
        Set<Integer> friends = user.getFriends();
        return friends.add(friendId);
    }


    private void removeFriend(Integer userId, Integer friendId) {
        User user = getById(userId);
        Set<Integer> friends = user.getFriends();
        if (friends.remove(friendId)) {
            log.info("Пользователь - {} был удален из друзей у - {}", userId, friendId);
        } else {
            log.debug("Пользователь - {} не был найден в списке друзей у - {}", friendId, userId);
        }
    }

    @Override
    public List<User> getListOfCommonFriends(Integer id, Integer otherId) {
        Set<Integer> friends = getById(id).getFriends();
        Set<Integer> friendsOtherUser = getById(otherId).getFriends();
        List<User> commonFriends = getAllUsers().stream()
                .filter(user1 -> friends.contains(user1.getId()))
                .filter(user2 -> friendsOtherUser.contains(user2.getId()))
                .collect(Collectors.toList());
        log.info("Запрос на получение общих друзей для : {} , {}  : {}", id, otherId, commonFriends);
        return commonFriends;
    }

    @Override
    public List<User> getListOfFriends(Integer id) {
        Set<Integer> friends = getById(id).getFriends();
        List<User> listOfFriends = getAllUsers().stream()
                .filter(user -> friends.contains(user.getId()))
                .collect(Collectors.toList());
        log.info("Запрос на получение списка друзей : {}", listOfFriends);
        return listOfFriends;
    }

    @Override
    public void deleteFriend(Integer id, Integer friendId) {
        User user = getById(id);
        User friend = getById(friendId);
        removeFriend(id, friendId);
        removeFriend(friendId, id);
    }

    @Override
    public void deleteUserById(Integer id) {

    }
}
