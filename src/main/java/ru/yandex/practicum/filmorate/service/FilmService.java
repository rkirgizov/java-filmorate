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

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;
    private final DirectorStorage directorStorage;
    private final UserStorage userStorage;

    public FilmService(@Qualifier("FilmStorageDbImpl") FilmStorage filmStorage, MpaStorage mpaStorage, GenreStorage genreStorage, DirectorStorage directorStorage, @Qualifier("UserStorageDbImpl") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
        this.directorStorage = directorStorage;
        this.userStorage = userStorage;

        checkGenreCount(6);
        checkMpaCount(5);
    }

    public FilmDto findFilmById(int filmId) {
        log.info("Поиск фильма по id: {}", filmId);
        return filmStorage.findFilmById(filmId)
                .map(film -> FilmMapper.mapToFilmDto(film, mpaStorage, genreStorage, directorStorage))
                .orElseThrow(() -> new NotFoundException(String.format("Фильм с id = %d не найден", filmId)));
    }

    public List<FilmDto> findAll() {
        log.info("Получение всех фильмов");
        return filmStorage.findAllFilms().stream()
                .map(film -> FilmMapper.mapToFilmDto(film, mpaStorage, genreStorage, directorStorage))
                .toList();
    }

    public FilmDto createFilm(FilmRequest filmRequest) {
        log.info("Создание фильма: {}", filmRequest);
        FilmRequest validatedFilmRequest = FilmValidator.validateFilmRequestNew(filmRequest, mpaStorage, genreStorage);
        FilmRequest validatedFilmRequest = FilmValidator.validateFilmRequestNew(filmRequest, mpaStorage, genreStorage, directorStorage);
        Film film = FilmMapper.mapToFilm(validatedFilmRequest);
        film = filmStorage.createFilm(film);

        return FilmMapper.mapToFilmDto(film, mpaStorage, genreStorage);
    }

    public FilmDto updateFilm(int filmId, FilmRequest filmRequest) {
        log.info("Обновление фильма с id {}: {}", filmId, filmRequest);
        Film existingFilm = filmStorage.findFilmById(filmId)
        Film filmForUpdate = filmStorage.findFilmById(filmId)
                .map(film -> FilmValidator.validateFilmRequestForUpdate(film, filmRequest, mpaStorage, genreStorage, directorStorage))
                .map(film -> FilmMapper.mapToFilm(filmRequest))
                .orElseThrow(() -> new NotFoundException(String.format("Фильм с id: %s не найден", filmId)));

        FilmRequest validatedFilmRequestForUpdate = FilmValidator.validateFilmRequestForUpdate(existingFilm, filmRequest, mpaStorage, genreStorage);
        Film filmToPersist = FilmMapper.mapToFilm(validatedFilmRequestForUpdate);
        filmToPersist.setId(filmId);

        Film filmUpdated = filmStorage.updateFilm(filmToPersist);

        return FilmMapper.mapToFilmDto(filmUpdated, mpaStorage, genreStorage, directorStorage);
    }


    public void addLikeToFilm(Integer filmId, Integer userId) {
        log.info("Пользователь {} ставит лайк фильму {}", userId, filmId);
        filmStorage.findFilmById(filmId)
                .orElseThrow(() -> new NotFoundException(String.format("Фильм с id: %s не найден", filmId)));
        userStorage.findUserById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id: %s не найден", userId)));
        filmStorage.addLikeToFilm(filmId, userId);
    }

    public void removeLikeFromFilm(Integer filmId, Integer userId) {
        log.info("Пользователь {} удаляет лайк с фильма {}", userId, filmId);
        filmStorage.findFilmById(filmId)
                .orElseThrow(() -> new NotFoundException(String.format("Фильм с id: %s не найден", filmId)));
        userStorage.findUserById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id: %s не найден", userId)));
        filmStorage.removeLikeFromFilm(filmId, userId);
    }

    public List<FilmDto> getPopularFilms(int limit) {
        log.info("Получение {} самых популярных фильмов", limit);
        List<Film> allFilms = filmStorage.getPopularFilms(limit);
        return allFilms.stream()
                .map(film -> FilmMapper.mapToFilmDto(film, mpaStorage, genreStorage, directorStorage))
                .toList();
    }

    public List<FilmDto> findCommonFilms(int userId, int friendId) {
        log.info("Поиск общих фильмов для пользователей {} и {}", userId, friendId);

        userStorage.findUserById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id: %s не найден", userId)));
        userStorage.findUserById(friendId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id: %s не найден", friendId)));

        List<Film> commonFilms = filmStorage.findCommonFilms(userId, friendId);

        List<FilmDto> commonFilmDtos = commonFilms.stream()
                .map(film -> FilmMapper.mapToFilmDto(film, mpaStorage, genreStorage))
                .collect(Collectors.toList());

        log.info("Найдено {} общих фильмов для пользователей {} и {}", commonFilmDtos.size(), userId, friendId);
        return commonFilmDtos;
    }

    public void checkGenreCount(Integer count) {
        log.info("Проверка количества жанров в БД. Ожидается: {}", count);
        if (!genreStorage.checkGenreCount(count)) {
            throw new NotFoundException("В справочнике жанров неверное количество жанров");
        }
        log.info("Проверка количества жанров пройдена успешно.");
    }

    public void checkMpaCount(Integer count) {
        log.info("Проверка количества рейтингов MPA в БД. Ожидается: {}", count);
        if (!mpaStorage.checkMpaCount(count)) {
            throw new NotFoundException("В справочнике возрастных рейтингов неверное количество рейтингов");
        }
        log.info("Проверка количества рейтингов MPA пройдена успешно.");
    }

    public List<FilmDto> getFilmsByDirectorSorted(int directorId, String sortBy) {
        // Получаем все фильмы режиссёра через storage
        List<Film> films = filmStorage.getFilmsByDirector(directorId);

        if (films.isEmpty()) {
            return Collections.emptyList();
        }

        // Сортировка
        switch (sortBy.toLowerCase()) {
            case "year":
                films.sort(Comparator.comparing(Film::getReleaseDate));
                break;
            case "likes":
                films.sort((f1, f2) -> {
                    int likes2 = filmStorage.countLikes(f2.getId());
                    int likes1 = filmStorage.countLikes(f1.getId());
                    return Integer.compare(likes2, likes1); // по убыванию
                });
                break;
            default:
                throw new IllegalArgumentException("Неподдерживаемый параметр сортировки: " + sortBy);
        }

        // Преобразуем в DTO и возвращаем
        return films.stream()
                .map(film -> FilmMapper.mapToFilmDto(film, mpaStorage, genreStorage, directorStorage))
                .toList();
    }

}