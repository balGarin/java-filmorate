package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Validated
public class UserController {
    private Map<Integer, User> users = new HashMap<>();

    final Logger log = LoggerFactory.getLogger(UserController.class);

    @PostMapping
    public User addUser(@RequestBody @Valid User newUser) {
        log.debug("Валидация успешно пройдена!");
        newUser.setId(generateId());
        if (newUser.getName() == null) {
            newUser.setName(newUser.getLogin());
            log.debug("Имя юзера не указано и было заменено на {}", newUser.getLogin());

        }
        users.put(newUser.getId(), newUser);
        log.info("Добавлен новый пользователь {}", newUser.toString());
        return newUser;
    }

    @PutMapping
    public User updateUser(@RequestBody @Valid User newUser) {
        log.debug("Валидация успешно пройдена!");
        log.info("Новый юзер для обновления {}", newUser.toString());
        if (newUser.getId() == null || !users.containsKey(newUser.getId())) {
            log.warn("Ошибка валидации : Некорректный Id");
            throw new ValidationException("Ошибка валидации : указан некорректный id");
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

    @GetMapping
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
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
