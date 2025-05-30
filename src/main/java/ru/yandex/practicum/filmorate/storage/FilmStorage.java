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

    List<Film> findCommonFilms(int userId, int friendId);

    List<Film> getFilmsByDirector(int directorId);

    int countLikes(int filmId);

    public void deleteDirectorsFromFilm(int filmId);

    public void addDirectorToFilm(int filmId, int directorId);
}