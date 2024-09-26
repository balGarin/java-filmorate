package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ComponentScan("ru.yandex.practicum.filmorate")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserRepositoryTest {

    private final UserRepository userRepository;


    @Test
    public void shouldCorrectAddUserAndFindById() {
        User user = new User();
        user.setLogin("login1");
        user.setEmail("garin@gmail.com");
        user.setBirthday(LocalDate.now());
        userRepository.addUser(user);
        assertEquals(user, userRepository.getById(1), "Юзер сохранился не верно");
    }


    @Test
    public void shouldCorrectReturnAllUsers() {
        User user1 = new User();
        user1.setLogin("login1");
        user1.setEmail("garin@gmail.com");
        user1.setBirthday(LocalDate.now());
        userRepository.addUser(user1);
        User user2 = new User();
        user2.setLogin("login1");
        user2.setEmail("garin@gmail.com");
        user2.setBirthday(LocalDate.now());
        userRepository.addUser(user2);
        List<User> users = userRepository.getAllUsers();
        assertEquals(2, users.size(), "Список не корректен");
    }


    @Test
    public void shouldCorrectUpdateUser() {
        User user = new User();
        user.setLogin("login1");
        user.setName("name");
        user.setEmail("garin@gmail.com");
        user.setBirthday(LocalDate.now());
        userRepository.addUser(user);
        user.setId(1);
        user.setLogin("newLogin");
        user.setName("newName");
        userRepository.updateUser(user);
        assertEquals(user, userRepository.getById(1), "Юзеры не совпадают");
    }


    @Test
    public void shouldCorrectAddAndDeleteFriends() {
        User user1 = new User();
        user1.setLogin("login1");
        user1.setEmail("garin@gmail.com");
        user1.setBirthday(LocalDate.now());
        userRepository.addUser(user1);
        User user2 = new User();
        user2.setLogin("login1");
        user2.setEmail("garin@gmail.com");
        user2.setBirthday(LocalDate.now());
        userRepository.addUser(user2);
        userRepository.addFriend(1, 2);
        assertEquals(1, userRepository.getListOfFriends(1).size(), "Количество друзей не верное");
        assertEquals(0, userRepository.getListOfFriends(2).size(), "У 2 юзера добавился друг");
        userRepository.deleteFriend(1, 2);
        assertEquals(0, userRepository.getListOfFriends(1).size(), "Друг не удалился");
    }


    @Test
    public void shouldCorrectReturnListOfFriends() {
        User user1 = new User();
        user1.setLogin("login1");
        user1.setEmail("garin@gmail.com");
        user1.setBirthday(LocalDate.now());
        userRepository.addUser(user1);
        User user2 = new User();
        user2.setLogin("login1");
        user2.setEmail("garin@gmail.com");
        user2.setBirthday(LocalDate.now());
        userRepository.addUser(user2);
        User user3 = new User();
        user3.setLogin("login1");
        user3.setEmail("garin@gmail.com");
        user3.setBirthday(LocalDate.now());
        userRepository.addUser(user3);
        userRepository.addFriend(1, 2);
        userRepository.addFriend(1, 3);
        List<User> users = List.of(user2, user3);
        List<User> usersFromDB = userRepository.getListOfFriends(1);
        assertEquals(users.size(), usersFromDB.size(), "Количество друзей не корректно");
        assertEquals(users, usersFromDB, "Список не корректен");
    }


    @Test
    public void shouldCorrectReturnListOfCommonFriends() {
        User user1 = new User();
        user1.setLogin("login1");
        user1.setEmail("garin@gmail.com");
        user1.setBirthday(LocalDate.now());
        userRepository.addUser(user1);
        User user2 = new User();
        user2.setLogin("login1");
        user2.setEmail("garin@gmail.com");
        user2.setBirthday(LocalDate.now());
        Set<Integer> friends = new HashSet<>();
        friends.add(2);
        user2.setFriends(friends);
        userRepository.addUser(user2);
        User user3 = new User();
        user3.setLogin("login1");
        user3.setEmail("garin@gmail.com");
        user3.setBirthday(LocalDate.now());
        userRepository.addUser(user3);
        userRepository.addFriend(1, 2);
        userRepository.addFriend(3, 2);
        List<User> users = userRepository.getListOfCommonFriends(1, 3);
        assertEquals(1, users.size(), "Количество друзей не корректно");
        assertEquals(user2, users.get(0), "Друзья отображаются не корректно");


    }

}