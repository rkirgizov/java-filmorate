package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.FilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.validation.FilmValidator;

import java.util.*;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;
    private final UserStorage userStorage;

    public FilmService(@Qualifier("FilmStorageDbImpl") FilmStorage filmStorage, MpaStorage mpaStorage, GenreStorage genreStorage, @Qualifier("UserStorageDbImpl") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
        this.userStorage = userStorage;

        // Проверка при инициализации приложения на количество записей в таблицах жанров и рейтингов
        checkGenreCount(6);
        checkMpaCount(5);
    }

    public FilmDto findFilmById(int filmId) {
        return filmStorage.findFilmById(filmId)
                .map(film -> FilmMapper.mapToFilmDto(film, mpaStorage, genreStorage))
                .orElseThrow(() -> new NotFoundException(String.format("Фильм с id = %d не найден", filmId)));
    }

    public List<FilmDto> findAll() {
        return filmStorage.findAllFilms().stream()
                .map(film -> FilmMapper.mapToFilmDto(film, mpaStorage, genreStorage))
                .toList();
    }

    public FilmDto createFilm(FilmRequest filmRequest) {
        FilmRequest validatedFilmRequest = FilmValidator.validateFilmRequestNew(filmRequest, mpaStorage, genreStorage);
        Film film = FilmMapper.mapToFilm(validatedFilmRequest);
        film = filmStorage.createFilm(film);

        return FilmMapper.mapToFilmDto(film, mpaStorage, genreStorage);
    }

    public FilmDto updateFilm(int filmId, FilmRequest filmRequest) {
        Film filmForUpdate = filmStorage.findFilmById(filmId)
                .map(film -> FilmValidator.validateFilmRequestForUpdate(film, filmRequest, mpaStorage, genreStorage))
                .map(film -> FilmMapper.mapToFilm(filmRequest))
                .orElseThrow(() -> new NotFoundException(String.format("Фильм с id: %s не найден", filmId)));
        filmForUpdate.setId(filmId);
        Film filmUpdated = filmStorage.updateFilm(filmForUpdate);

        return FilmMapper.mapToFilmDto(filmUpdated, mpaStorage, genreStorage);
    }

    public void addLikeToFilm(Integer filmId, Integer userId) {
        filmStorage.findFilmById(filmId)
                .orElseThrow(() -> new NotFoundException(String.format("Фильм с id: %s не найден", filmId)));
        userStorage.findUserById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id: %s не найден", userId)));
        filmStorage.addLikeToFilm(filmId, userId);
    }

    public void removeLikeFromFilm(Integer filmId, Integer userId) {
        filmStorage.findFilmById(filmId)
                .orElseThrow(() -> new NotFoundException(String.format("Фильм с id: %s не найден", filmId)));
        userStorage.findUserById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id: %s не найден", userId)));
        filmStorage.removeLikeFromFilm(filmId, userId);
    }

    public List<FilmDto> getPopularFilms(int limit) {
        List<Film> allFilms = filmStorage.getPopularFilms(limit);
        return allFilms.stream()
                .map(film -> FilmMapper.mapToFilmDto(film, mpaStorage, genreStorage))
                .toList();
    }


    /**
     * Проверяем при инициализации приложения ожидаемое количество жанров и возрастных рейтингов в хранилище.
     * Если количество не соответствует ожидаемому, то программа не будет работать корректно,
     * поэтому выбрасываем исключение NotFoundException.
     *
     * @param count ожидаемое количество жанров
     */

    public void checkGenreCount(Integer count) {
        if (!genreStorage.checkGenreCount(count)) {
            throw new NotFoundException("В справочнике жанров неверное количество жанров");
        }
    }

    public void checkMpaCount(Integer count) {
        if (!mpaStorage.checkMpaCount(count)) {
            throw new NotFoundException("В справочнике возрастных рейтингов неверное количество рейтингов");
        }
    }

}