package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Getter
@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    protected final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        Film validFilm = validateFilm(film);
        validFilm.setId(getNextId());
        films.put(validFilm.getId(), validFilm);
        log.info("Фильм с id = {}  - добавлен", validFilm.getId());
        return validFilm;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        if (newFilm.getId() == null) {
            throw new ValidationException("Для обновления фильма необходимо указать id");
        }
        if (films.containsKey(newFilm.getId())) {
            Film validFilm = validateFilm(newFilm);
            Film oldFilm = films.get(validFilm.getId());
            oldFilm.setName(validFilm.getName());
            oldFilm.setDescription(validFilm.getDescription());
            oldFilm.setReleaseDate(validFilm.getReleaseDate());
            oldFilm.setDuration(validFilm.getDuration());
            log.info("Фильм \"{}\" с id = {}  - обновлен", validFilm.getName(), validFilm.getId());
            return oldFilm;
        }
        throw new NotFoundException(String.format("Фильм с id = %d  - не найден", newFilm.getId()));
    }

    private Film validateFilm(Film film) {
        for (Film value : films.values()) {
            if (film.getName().equals(value.getName())) {
                throw new ValidationException("Фильм с таким названием уже существует в фильмотеке");
            }
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        return film;
    }

    private long getNextId() {
        long currentMaxId = films.keySet().stream().mapToLong(id -> id).max().orElse(0);
        return ++currentMaxId;
    }

}