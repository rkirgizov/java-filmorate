package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.*;

@Slf4j
@Component
public class FilmStorageInMemory implements FilmStorage {

    private final Map<Integer, Film> films = new HashMap<>();
    private final Map<Integer, Set<Integer>> filmLikes = new HashMap<>();

    @Override
    public Optional<Film> findFilmById(int filmId) {
        return Optional.ofNullable(films.get(filmId));
    }

    @Override
    public Film createFilm(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.debug("Фильм с id = {}  - добавлен", film);
        return film;
    }

    @Override
    public Film updateFilm(Film updatedFilm) {
        if (updatedFilm.getId() == null) {
            throw new ValidationException("Для обновления фильма необходимо указать id");
        }
        if (films.containsKey(updatedFilm.getId())) {
            Film oldFilm = films.get(updatedFilm.getId());
            oldFilm.setName(updatedFilm.getName());
            oldFilm.setDescription(updatedFilm.getDescription());
            oldFilm.setReleaseDate(updatedFilm.getReleaseDate());
            oldFilm.setDuration(updatedFilm.getDuration());
            oldFilm.setDirectors(updatedFilm.getDirectors());
            return oldFilm;
        }
        throw new NotFoundException(String.format("Фильм с id = %d  - не найден", updatedFilm.getId()));
    }

    @Override
    public void addLikeToFilm(Integer filmId, Integer userId) {
        filmLikes.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
    }

    @Override
    public void removeLikeFromFilm(Integer filmId, Integer userId) {
        Set<Integer> likes = filmLikes.get(filmId);
        if (likes != null) {
            likes.remove(userId);
        }
    }

    @Override
    public List<Film> getPopularFilms(Integer limit) {
        return List.of();
    }

    @Override
    public List<Film> findAllFilms() {
        return new ArrayList<>(films.values());
    }

    private int getNextId() {
        int currentMaxId = films.keySet().stream().mapToInt(id -> id).max().orElse(0);
        return ++currentMaxId;
    }

    @Override
    public List<Film> getFilmsByDirector(int directorId) {
        return films.values().stream()
                .filter(film -> film.getDirectors() != null && film.getDirectors().contains(directorId))
                .toList();
    }

    @Override
    public int countLikes(int filmId) {
        return filmLikes.getOrDefault(filmId, Collections.emptySet()).size();
    }

    public List<Film> findCommonFilms(int userId, int friendId) {
        log.warn("In-memory FilmStorage does not fully support common films logic. Returning empty list.");

        return new ArrayList<>();
    }
}