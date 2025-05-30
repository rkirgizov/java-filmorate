package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    @GetMapping("/{id}")
    public FilmDto findFilmById(@PathVariable("id") Integer id) {
        return filmService.findFilmById(id);
    }

    @GetMapping
    public List<FilmDto> findAll() {
        return filmService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FilmDto createFilm(@Valid @RequestBody FilmRequest filmRequest) {
        return filmService.createFilm(filmRequest);
    }

    @PutMapping
    public FilmDto updateFilm(@Valid @RequestBody FilmRequestUpdate filmRequestUpdate) {
        return filmService.updateFilm(filmRequestUpdate.getId(), filmRequestUpdate);
    }

    @PutMapping("/{filmId}/like/{userId}")
    public void addLikeToFilm(@PathVariable("filmId") Integer filmId, @PathVariable("userId") Integer userId) {
        filmService.addLikeToFilm(filmId, userId);
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public void removeLikeFromFilm(@PathVariable("filmId") Integer filmId, @PathVariable("userId") Integer userId) {
        filmService.removeLikeFromFilm(filmId, userId);
    }

    @GetMapping("/popular")
    public List<FilmDto> getPopularFilms(@RequestParam(name = "limit", required = false, defaultValue = "10") Integer limit) {
        return filmService.getPopularFilms(limit);
    }

    @GetMapping("/director/{directorId}")
    public List<FilmDto> getFilmsByDirector(@PathVariable int directorId,
                                            @RequestParam(name = "sortBy") String sortBy) {
        return filmService.getFilmsByDirectorSorted(directorId, sortBy);
    }

}