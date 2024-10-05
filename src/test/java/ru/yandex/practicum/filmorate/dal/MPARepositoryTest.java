package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.MPA;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ComponentScan("ru.yandex.practicum.filmorate")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class MPARepositoryTest {

    private final MPARepository mpaRepository;


    @Test
    public void shouldCorrectReturnRatingByIdAndAll() {
        MPA mpaG = new MPA();
        mpaG.setId(1);
        mpaG.setName("G");
        assertEquals(mpaG, mpaRepository.getMPAByID(1), "Рейтинг возвращается не корректно");
        MPA mpaPG = new MPA();
        mpaPG.setId(2);
        mpaPG.setName("PG");
        MPA pg13 = new MPA();
        pg13.setId(3);
        pg13.setName("PG-13");
        MPA mpaR = new MPA();
        mpaR.setId(4);
        mpaR.setName("R");
        MPA nc17 = new MPA();
        nc17.setId(5);
        nc17.setName("NC-17");
        List<MPA> ratings = List.of(mpaG, mpaPG, pg13, mpaR, nc17);
        assertEquals(ratings.size(), mpaRepository.getAllMPA().size(), "Количество не корректно");
        assertEquals(ratings, mpaRepository.getAllMPA(), "Рейтинги отображаются не корректно");
    }

}