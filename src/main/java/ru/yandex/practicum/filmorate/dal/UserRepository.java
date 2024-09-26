package ru.yandex.practicum.filmorate.dal;

import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FriendRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.StatusRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.IncorrectDataException;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@AllArgsConstructor
@Repository("DBUsers")
public class UserRepository implements UserStorage {
    private final JdbcTemplate jdbc;

    private final UserRowMapper userRowMapper;
    private final FriendRowMapper friendRowMapper;
    private final StatusRowMapper statusRowMapper;

    private static final String ADD_USER = "INSERT INTO USERS (EMAIL, LOGIN, USER_NAME, BIRTHDAY)" +
            "VALUES(?, ?, ?, ?)";

    private static final String UPDATE_USER = "UPDATE USERS " +
            "SET EMAIL = ?, LOGIN = ?, USER_NAME = ?, BIRTHDAY = ? " +
            "WHERE USER_ID = ?";

    private static final String FIND_USER_BY_ID = "SELECT * FROM USERS WHERE USER_ID = ?";

    private static final String FIND_ALL_USERS = "SELECT * FROM USERS";

    private static final String ADD_FRIEND = "INSERT INTO PUBLIC.FRIENDS " +
            "(USER_ID, FRIEND_ID, STATUS_ID) " +
            "VALUES(?, ?, ?)";

    private static final String GET_FRIENDS = "SELECT FRIEND_ID " +
            "FROM FRIENDS " +
            "WHERE USER_ID = ?";

    private static final String DELETE_FRIEND = "DELETE FROM FRIENDS " +
            "WHERE USER_ID = ? AND FRIEND_ID = ?";

    private static final String GET_STATUS = "SELECT STATUS_ID FROM FRIENDS " +
            "WHERE USER_ID = ? AND FRIEND_ID = ?";

    private static final String UPDATE_STATUS = "UPDATE FRIENDS " +
            "SET STATUS_ID= ? " +
            "WHERE USER_ID= ? AND FRIEND_ID = ?";

    @Override
    public User addUser(User newUser) {
        Integer id = insert(ADD_USER, newUser.getEmail(), newUser.getLogin(), newUser.getName(), newUser.getBirthday());
        if (newUser.getName() == null) {
            newUser.setName(newUser.getLogin());
        }
        newUser.setId(id);

        if (newUser.getFriends() == null) {
            newUser.setFriends(new HashSet<>());
        }
        return newUser;
    }

    @Override
    public User updateUser(User newUser) {
        if (newUser.getId() == null) {
            throw new IncorrectDataException("ID обязателен для этой операции");
        }
        User user = getById(newUser.getId());
        jdbc.update(UPDATE_USER, newUser.getEmail(), newUser.getLogin(), newUser.getName(), newUser.getBirthday(),
                user.getId());
        user = getById(newUser.getId());
        List<Integer> friends = jdbc.query(GET_FRIENDS, friendRowMapper, user.getId());
        user.setFriends(new HashSet<>(friends));
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        List<User> users = jdbc.query(FIND_ALL_USERS, userRowMapper);
        for (User user : users) {
            if (user.getName() == null) {
                user.setName(user.getLogin());
            }
            List<Integer> friends = jdbc.query(GET_FRIENDS, friendRowMapper, user.getId());
            user.setFriends(new HashSet<>(friends));
        }
        return users;
    }

    @Override
    public User getById(Integer id) {
        try {
            User user = jdbc.queryForObject(FIND_USER_BY_ID, userRowMapper, id);
            List<Integer> friends = jdbc.query(GET_FRIENDS, friendRowMapper, id);
            user.setFriends(new HashSet<>(friends));
            if (user.getName() == null) {
                user.setName(user.getLogin());
            }
            return user;
        } catch (DataAccessException e) {
            throw new NotFoundException("Пользователь с " + id + " id не найден");
        }
    }

    @Override
    public void addFriend(Integer id, Integer friendId) {
        try {
            getById(id);
        } catch (DataAccessException e) {
            throw new NotFoundException("Пользователь с ID = " + id + " не найден");
        }
        try {
            getById(friendId);
        } catch (DataAccessException e) {
            throw new NotFoundException("Пользователь с ID = " + friendId + " не найден");
        }
        if (checkFriendship(id, friendId)) {
            throw new IncorrectDataException("Пользователь с ID " + friendId + " уже в друзьях у пользователя с ID "
                    + id);
        }
        if (checkFriendship(friendId, id)) {
            insertForTwoKeys(ADD_FRIEND, id, friendId, 1);
        } else {
            insertForTwoKeys(ADD_FRIEND, id, friendId, 2);

        }
    }

    @Override
    public void deleteFriend(Integer id, Integer friendId) {
        try {
            getById(id);
        } catch (DataAccessException e) {
            throw new NotFoundException("Пользователь с ID = " + id + " не найден");
        }
        try {
            getById(friendId);
        } catch (DataAccessException e) {
            throw new NotFoundException("Пользователь с ID = " + friendId + " не найден");
        }
        if (!checkFriendship(id, friendId)) {
            return;
        }
        Integer status = jdbc.queryForObject(GET_STATUS, statusRowMapper, id, friendId);
        if (status.equals(1)) {
            jdbc.update(UPDATE_STATUS, 2, friendId, id);
        }
        jdbc.update(DELETE_FRIEND, id, friendId);
    }

    @Override
    public List<User> getListOfFriends(Integer id) {
        getById(id);
        List<Integer> friendsId = jdbc.query(GET_FRIENDS, friendRowMapper, id);
        List<User> friends = new ArrayList<>();
        for (Integer friendId : friendsId) {
            User user = jdbc.queryForObject(FIND_USER_BY_ID, userRowMapper, friendId);
            if (user.getName() == null) {
                user.setName(user.getLogin());
            }
            List<Integer> userId = jdbc.query(GET_FRIENDS, friendRowMapper, friendId);
            user.setFriends(new HashSet<>(userId));
            friends.add(user);
        }
        return friends;


    }

    @Override
    public List<User> getListOfCommonFriends(Integer userid, Integer otherId) {
        List<Integer> listOfUserFriends = jdbc.query(GET_FRIENDS, friendRowMapper, userid);
        List<Integer> listOfFriendFriends = jdbc.query(GET_FRIENDS, friendRowMapper, otherId);
        List<User> users = jdbc.query(FIND_ALL_USERS, userRowMapper);
        for (User us : users) {
            if (us.getName() == null) {
                us.setName(us.getLogin());
            }
            List<Integer> userId = jdbc.query(GET_FRIENDS, friendRowMapper, otherId);
            us.setFriends(new HashSet<>(userId));
        }
        return users.stream()
                .filter(user -> listOfUserFriends.contains(user.getId()))
                .filter(user -> listOfFriendFriends.contains(user.getId()))
                .toList();
    }

    private Integer insert(String query, Object... params) {
        try {


            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
            jdbc.update(connection -> {
                PreparedStatement ps = connection
                        .prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                for (int idx = 0; idx < params.length; idx++) {
                    ps.setObject(idx + 1, params[idx]);
                }
                return ps;
            }, keyHolder);

            Integer id = keyHolder.getKeyAs(Integer.class);


            if (id != null) {
                return id;
            } else {
                throw new InternalServerException("Не удалось сохранить данные");
            }
        } catch (DataAccessException e) {
            throw new ValidationException(e.getMessage());
        }
    }

    private void insertForTwoKeys(String query, Object... params) {
        try {


            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
            jdbc.update(connection -> {
                PreparedStatement ps = connection
                        .prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                for (int idx = 0; idx < params.length; idx++) {
                    ps.setObject(idx + 1, params[idx]);
                }
                return ps;
            }, keyHolder);
        } catch (DataAccessException e) {
            throw new ValidationException(e.getMessage());
        }
    }

    private boolean checkFriendship(Integer userId, Integer friendId) {
        List<Integer> friendsOfUser = jdbc.query(GET_FRIENDS, friendRowMapper, userId);
        return friendsOfUser.stream()
                .anyMatch(id -> id.equals(friendId));
    }
}
