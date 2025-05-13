package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Optional<Film> findFilmById(int filmId);

    List<Film> findAllFilms();

    Film createFilm(Film film);

    Film updateFilm(Film filmForUpdate);

    void addLikeToFilm(Integer filmId, Integer userId);

    void removeLikeFromFilm(Integer filmId, Integer userId);

    List<Film> getPopularFilms(Integer userId);
}