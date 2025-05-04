package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    @GetMapping("/{id}")
    public Film findFilmById(@PathVariable("id") Long id) {
        return filmService.findFilmById(id);
    }

    @GetMapping
    public Collection<Film> findAll() {
        return filmService.findAll();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        return filmService.createFilm(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        return filmService.updateFilm(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLikeToFilm(@PathVariable("id") Long id, @PathVariable("userId") Long userId) {
        filmService.addLikeToFilm(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLikeFromFilm(@PathVariable("id") Long id, @PathVariable("userId") Long userId) {
        filmService.removeLikeFromFilm(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(name = "count", required = false, defaultValue = "10") Integer count) {
        return filmService.getPopularFilms(count);
    }

}