package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.MPARowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MPA;

import java.util.List;
@Repository
@RequiredArgsConstructor
public class MPARepository {
    private  static final String FIND_ALL_MPA = "SELECT * FROM RATINGS ";
    private static final String FIND_MPA_BY_ID = "SELECT * FROM RATINGS WHERE RATING_ID = ?";



    private final JdbcTemplate jdbc;
    private final MPARowMapper mapper;

    public List<MPA> getAllMPA(){
        return jdbc.query(FIND_ALL_MPA,mapper);
    }


    public  MPA getMPAByID(Integer id) {
        try {
            return jdbc.queryForObject(FIND_MPA_BY_ID,mapper,id);
        }catch (EmptyResultDataAccessException e){
            throw new NotFoundException("Id рейтинга не найден ");
        }
    }
}
