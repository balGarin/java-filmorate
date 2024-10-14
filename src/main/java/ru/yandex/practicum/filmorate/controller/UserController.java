package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
@Validated
@AllArgsConstructor
public class UserController {
    private final UserService userService;
    private final FilmService filmService;


    @PostMapping
    public User addUser(@RequestBody @Valid User newUser) {
        return userService.addUser(newUser);
    }

    @PutMapping
    public User updateUser(@RequestBody @Valid User newUser) {
        return userService.updateUser(newUser);
    }

    @GetMapping
    public List<User> getUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable Integer id) {
        return userService.getById(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        userService.deleteFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getListOfFriends(@PathVariable Integer id) {
        return userService.getListOfFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getListOfCommonFriends(@PathVariable Integer id, @PathVariable Integer otherId) {
        return userService.getListOfCommonFriends(id, otherId);
    }

    /**
     * Вывод списка фильмов рекомендованных на основе лайков других пользователей
     *
     * @param id полльзователя которму даются рекомендации
     * @return возврщает список фильмов
     */
    @GetMapping("/{id}/recommendations")
    public List<Film> getRecommendations(@PathVariable Long id) {
        return filmService.getRecommendations(id);
    }

    @DeleteMapping("/{id}")
    public void deleteUserByID(@PathVariable Integer id) {
        userService.deleteUserById(id);
    }
}
