package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.FilmRequest;
import ru.yandex.practicum.filmorate.dto.FilmRequestUpdate;
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
        log.info("Получен запрос GET /films/{} для поиска фильма", id);
        return filmService.findFilmById(id);
    }

    @GetMapping
    public List<FilmDto> findAll() {
        log.info("Получен запрос GET /films для получения всех фильмов");
        return filmService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FilmDto createFilm(@Valid @RequestBody FilmRequest filmRequest) {
        log.info("Получен запрос POST /films для создания фильма: {}", filmRequest);
        return filmService.createFilm(filmRequest);
    }

    @PutMapping
    public FilmDto updateFilm(@Valid @RequestBody FilmRequestUpdate filmRequestUpdate) {
        log.info("Получен запрос PUT /films для обновления фильма с id {}: {}", filmRequestUpdate.getId(), filmRequestUpdate);
        return filmService.updateFilm(filmRequestUpdate.getId(), filmRequestUpdate);
    }

    @PutMapping("/{filmId}/like/{userId}")
    public void addLikeToFilm(@PathVariable("filmId") Integer filmId, @PathVariable("userId") Integer userId) {
        log.info("Получен запрос PUT /films/{}/like/{} для добавления лайка", filmId, userId);
        filmService.addLikeToFilm(filmId, userId);
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public void removeLikeFromFilm(@PathVariable("filmId") Integer filmId, @PathVariable("userId") Integer userId) {
        log.info("Получен запрос DELETE /films/{}/like/{} для удаления лайка", filmId, userId);
        filmService.removeLikeFromFilm(filmId, userId);
    }

    @GetMapping("/popular")
    @ResponseStatus(HttpStatus.OK)
    public List<FilmDto> getPopularFilms(
            @RequestParam(name = "count", required = false, defaultValue = "10") @Positive Integer count,
            @RequestParam(name = "genreId", required = false) @Positive Integer genreId,
            @RequestParam(name = "year", required = false) Integer year
    ) {
        log.info("Получен запрос GET /films/popular с параметрами count={}, genreId={}, year={}", count, genreId, year);
        return filmService.getPopularFilms(count, genreId, year);
    }

    @GetMapping("/director/{directorId}")
    public List<FilmDto> getFilmsByDirector(@PathVariable int directorId,
                                            @RequestParam(name = "sortBy") String sortBy) {
        return filmService.getFilmsByDirectorSorted(directorId, sortBy);
    }

    @GetMapping("/common")
    @ResponseStatus(HttpStatus.OK)
    public List<FilmDto> getCommonFilms(
            @RequestParam @Positive int userId,
            @RequestParam @Positive int friendId
    ) {
        log.info("Получен запрос GET /films/common с параметрами userId={} и friendId={}", userId, friendId);
        List<FilmDto> commonFilmDtos = filmService.findCommonFilms(userId, friendId);
        log.info("Возвращен список из {} общих фильмов для пользователей {} и {}", commonFilmDtos.size(), userId, friendId);
        return commonFilmDtos;
    }

    @GetMapping("/search")
    public List<FilmDto> searchFilms(
            @RequestParam String query,
            @RequestParam(defaultValue = "title,director") List<String> by) {
        log.info("Получен запрос GET /films/search с параметрами query={}, by={}", query, by);
        return filmService.searchFilms(query, by);
    }

    @DeleteMapping("/{filmId}")
    public void removeFilm(@PathVariable Integer filmId) {
        log.debug("Получен запрос DELETE /films/{} для удаления фильма", filmId);
        filmService.removeFilm(filmId);
    }
}