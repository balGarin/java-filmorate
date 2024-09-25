package ru.yandex.practicum.filmorate.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dal.MPARepository;
import ru.yandex.practicum.filmorate.model.MPA;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@AllArgsConstructor
public class MPAController {

    private final MPARepository mpaRepository;

    @GetMapping
    public List<MPA> getAllMPA() {
        return mpaRepository.getAllMPA();
    }

    @GetMapping("/{id}")
    public MPA getMPAByID(@PathVariable Integer id) {
        return  mpaRepository.getMPAByID(id);
    }

}
