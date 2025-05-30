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

    List<Film> getPopularFilms(int count, Integer genreId, Integer year);

    List<Film> findCommonFilms(int userId, int friendId);

    List<Film> getFilmsByDirector(int directorId);

    int countLikes(int filmId);

    void deleteDirectorsFromFilm(int filmId);

    void addDirectorToFilm(int filmId, int directorId);

    List<Film> searchFilms(String query, boolean searchByTitle, boolean searchByDirector);

    void deleteFilm(int filmId); //
}