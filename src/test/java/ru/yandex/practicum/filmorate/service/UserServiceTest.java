package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.IncorrectDataException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    @Test
    public void shouldCorrectAddToFriends() {
        InMemoryUserStorage storage = new InMemoryUserStorage();
        UserService service = new UserService(storage);
        User user = new User();
        user.setLogin("login");
        user.setEmail("yandex@gmai.ru");
        storage.addUser(user);
        User friend = new User();
        friend.setLogin("login");
        friend.setEmail("ss@mail.ru");
        storage.addUser(friend);
        service.addFriend(user.getId(), friend.getId());
        assertEquals(1, user.getFriends().size(), "Добавление в друзья работает не корректно");
        assertEquals(1, friend.getFriends().size(),
                "У второго пользователя, добавление работает не корректно ");

    }

    @Test
    public void shouldThrowCorrectExceptionWhileByAddFriend() {
        InMemoryUserStorage storage = new InMemoryUserStorage();
        UserService service = new UserService(storage);
        User user = new User();
        user.setLogin("login");
        user.setEmail("yandex@gmai.ru");
        storage.addUser(user);
        boolean exceptionThrown = false;
        try {
            service.addFriend(user.getId(), user.getId());
        } catch (IncorrectDataException e) {
            exceptionThrown = true;
            assertEquals("Нельзя добавить в друзья самого себя", e.getMessage(), "Не верное сообщение");
        }
        assertTrue(exceptionThrown, "Исключение не было выброшено");
        User friend = new User();
        friend.setLogin("log");
        friend.setEmail("aa.mail.ru");
        storage.addUser(friend);
        service.addFriend(user.getId(), friend.getId());
        exceptionThrown = false;
        try {
            service.addFriend(user.getId(), friend.getId());
        } catch (IncorrectDataException e) {
            exceptionThrown = true;
            assertEquals("Пользователи уже являются друзьями!", e.getMessage(), "Не верное сообщение");
        }
        assertTrue(exceptionThrown, "Исключение не было выброшено");
    }

    @Test
    public void shouldCorrectDeleteFromFriends() {
        InMemoryUserStorage storage = new InMemoryUserStorage();
        UserService service = new UserService(storage);
        User user = new User();
        user.setLogin("login");
        user.setEmail("yandex@gmai.ru");
        storage.addUser(user);
        User friend = new User();
        friend.setLogin("login");
        friend.setEmail("ss@mail.ru");
        storage.addUser(friend);
        service.addFriend(user.getId(), friend.getId());
        service.deleteFriend(user.getId(), friend.getId());
        assertEquals(0, user.getFriends().size(), "У пользователя номер 1 друг не удалился");
        assertEquals(0, friend.getFriends().size(), "У пользователя номер 2 друг не удалился");
    }

    @Test
    public void shouldCorrectMappingCommonFriends() {
        InMemoryUserStorage storage = new InMemoryUserStorage();
        UserService service = new UserService(storage);
        User user1 = new User();
        user1.setLogin("login");
        user1.setEmail("yandex@gmai.ru");
        storage.addUser(user1);
        User user2 = new User();
        user2.setLogin("login");
        user2.setEmail("ss@mail.ru");
        storage.addUser(user2);
        User user3 = new User();
        user1.setLogin("login");
        user1.setEmail("yandex@gmai.ru");
        storage.addUser(user3);
        User user4 = new User();
        user1.setLogin("login");
        user1.setEmail("yandex@gmai.ru");
        storage.addUser(user4);
        service.addFriend(user1.getId(), user2.getId());
        service.addFriend(user1.getId(), user3.getId());
        service.addFriend(user4.getId(), user3.getId());
        assertNotEquals(user1.getFriends(), user4.getFriends(), "Количество друзей отображается не корректно");
        assertEquals(1, service.getListOfCommonFriends(user1.getId(), user4.getId()).size(),
                "Список общих друзей не корректен");
    }

}