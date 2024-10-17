package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dal.DirectorRepository;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;


@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {

    private final DirectorRepository directorRepository;

    @GetMapping
    public List<Director> getAllDirectors() {
        return directorRepository.getAllDirectors();
    }

    @GetMapping("/{id}")
    public Director getDirectorById(@PathVariable Integer id) {
        return directorRepository.getDirectorById(id);
    }

    @PostMapping
    public Director addDirector(@Valid @RequestBody Director newDirector) {
        return directorRepository.addDirector(newDirector);
    }

    @PutMapping
    public Director updateDirector(@RequestBody Director newDirector) {
        return directorRepository.updateDirector(newDirector);
    }

    @DeleteMapping("/{id}")
    public void deleteDirector(@PathVariable Integer id) {
        directorRepository.deleteDirectorById(id);
    }
}
