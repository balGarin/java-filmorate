package ru.yandex.practicum.filmorate.storage;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.IncorrectDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private Map<Integer, User> users = new HashMap<>();


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
        log.info("Добавлен новый пользователь {}", newUser.toString());
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
        log.info("Пользователь № {} успешно обновлен", user.toString());
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
}
