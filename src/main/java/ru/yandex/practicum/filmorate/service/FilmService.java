package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.FilmRequest;
import ru.yandex.practicum.filmorate.enumeration.EventOperation;
import ru.yandex.practicum.filmorate.enumeration.EventType;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.*;
import ru.yandex.practicum.filmorate.validation.FilmValidator;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;
    private final DirectorStorage directorStorage;
    private final UserStorage userStorage;

    private final Map<Integer, Mpa> allMpaRatings;

    @Autowired
    public FilmService(@Qualifier("FilmStorageDbImpl") FilmStorage filmStorage, MpaStorage mpaStorage, GenreStorage genreStorage, DirectorStorage directorStorage, @Qualifier("UserStorageDbImpl") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
        this.directorStorage = directorStorage;
        this.userStorage = userStorage;

        this.allMpaRatings = mpaStorage.findAllMpa().stream()
                .collect(Collectors.toMap(Mpa::getId, m -> m));

        checkGenreCount(6);
        checkMpaCount(5);
    }

    private void checkUserById(int userId) {
        userStorage.findUserById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id: %s не найден", userId)));
    }

    private Film getFilmById(int filmId) {
        return filmStorage.findFilmById(filmId)
                .orElseThrow(() -> new NotFoundException(String.format("Фильм с id = %d не найден", filmId)));
    }

    private void checkDirectorById(int directorId) {
        directorStorage.findDirectorById(directorId)
                .orElseThrow(() -> new NotFoundException(String.format("Режиссер с id = %d не найден", directorId)));
    }

    private List<FilmDto> mapFilmListToDto(List<Film> films) {
        if (films == null || films.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> filmIds = films.stream()
                .map(Film::getId)
                .filter(Objects::nonNull)
                .toList();

        if (filmIds.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Integer, List<Genre>> genresMap = genreStorage.getGenresMapByFilmIds(filmIds);

        Map<Integer, List<Director>> directorsMap = directorStorage.getDirectorsMapByFilmIds(filmIds);


        return films.stream()
                .map(film -> {
                    Mpa filmMpa = null;
                    if (film.getMpa() != null && film.getMpa().getId() != null) {
                        filmMpa = allMpaRatings.get(film.getMpa().getId());
                    }
                    List<Genre> filmGenres = genresMap.getOrDefault(film.getId(), Collections.emptyList());
                    List<Director> filmDirectors = directorsMap.getOrDefault(film.getId(), Collections.emptyList());

                    return FilmMapper.mapToFilmDto(film, filmMpa, filmGenres, filmDirectors);
                })
                .collect(Collectors.toList());
    }


    public FilmDto findFilmById(int filmId) {
        log.info("Поиск фильма по id: {}", filmId);
        Film film = getFilmById(filmId);

        Mpa filmMpa = null;
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            filmMpa = allMpaRatings.get(film.getMpa().getId());
        }

        List<Integer> genreIds = (film.getGenres() != null) ? new ArrayList<>(film.getGenres()) : Collections.emptyList();
        List<Genre> filmGenres = genreStorage.findGenresByIds(genreIds);

        List<Integer> directorIds = (film.getDirectors() != null) ? new ArrayList<>(film.getDirectors()) : Collections.emptyList();
        List<Director> filmDirectors = directorStorage.findDirectorsByIds(directorIds);


        return FilmMapper.mapToFilmDto(film, filmMpa, filmGenres, filmDirectors);
    }

    public List<FilmDto> findAll() {
        log.info("Получение всех фильмов");
        List<Film> films = filmStorage.findAllFilms();

        return mapFilmListToDto(films);
    }

    public FilmDto createFilm(FilmRequest filmRequest) {
        log.info("Создание фильма: {}", filmRequest);
        FilmRequest validatedFilmRequest = FilmValidator.validateFilmRequestNew(filmRequest, mpaStorage, genreStorage, directorStorage);

        Film film = FilmMapper.mapToFilm(validatedFilmRequest);

        film = filmStorage.createFilm(film);

        return findFilmById(film.getId());
    }

    public FilmDto updateFilm(int filmId, FilmRequest filmRequest) {
        log.info("Обновление фильма с id {}: {}", filmId, filmRequest);
        Film existingFilm = getFilmById(filmId);

        FilmRequest validatedFilmRequestForUpdate = FilmValidator.validateFilmRequestForUpdate(existingFilm, filmRequest, mpaStorage, genreStorage, directorStorage);

        Film filmToPersist = FilmMapper.mapToFilm(validatedFilmRequestForUpdate);
        filmToPersist.setId(filmId);

        Film filmUpdated = filmStorage.updateFilm(filmToPersist);

        return findFilmById(filmUpdated.getId());
    }

    public void addLikeToFilm(Integer filmId, Integer userId) {
        getFilmById(filmId);
        checkUserById(userId);
        log.info("Пользователь {} ставит лайк фильму {}", userId, filmId);
        filmStorage.addLikeToFilm(filmId, userId);
        userStorage.addEvent(userId, EventType.LIKE, EventOperation.ADD, filmId);
    }

    public void removeLikeFromFilm(Integer filmId, Integer userId) {
        log.info("Пользователь {} удаляет лайк с фильма {}", userId, filmId);
        getFilmById(filmId);
        checkUserById(userId);
        filmStorage.removeLikeFromFilm(filmId, userId);
        userStorage.addEvent(userId, EventType.LIKE, EventOperation.REMOVE, filmId);
    }

    public List<FilmDto> getPopularFilms(int count, Integer genreId, Integer year) {
        log.info("Получение {} самых популярных фильмов, фильтрация: genreId={}, year={}", count, genreId, year);

        FilmValidator.validateGenreIdForFilter(genreId, genreStorage);
        FilmValidator.validateYearForFilter(year);

        List<Film> popularFilms = filmStorage.getPopularFilms(count, genreId, year);

        return mapFilmListToDto(popularFilms);
    }

    public List<FilmDto> findCommonFilms(int userId, int friendId) {
        log.info("Поиск общих фильмов для пользователей {} и {}", userId, friendId);

        checkUserById(userId);
        checkUserById(friendId);

        List<Film> commonFilms = filmStorage.findCommonFilms(userId, friendId);

        return mapFilmListToDto(commonFilms);
    }

    public void checkGenreCount(Integer count) {
        log.info("Проверка количества жанров в БД. Ожидается: {}", count);
        if (allMpaRatings.size() != count) {
            log.warn("Количество MPA в памяти ({}) не совпадает с ожидаемым ({})", allMpaRatings.size(), count);
            if (!genreStorage.checkGenreCount(count)) {
                throw new NotFoundException("В справочнике жанров неверное количество жанров");
            }
        } else {
            log.info("Проверка количества жанров пройдена успешно (по памяти).");
            return;
        }
        log.info("Проверка количества жанров пройдена успешно (по БД).");
    }

    public void checkMpaCount(Integer count) {
        log.info("Проверка количества рейтингов MPA в БД. Ожидается: {}", count);
        if (allMpaRatings.size() != count) {
            log.warn("Количество MPA в памяти ({}) не совпадает с ожидаемым ({})", allMpaRatings.size(), count);
            if (!mpaStorage.checkMpaCount(count)) {
                throw new NotFoundException("В справочнике возрастных рейтингов неверное количество рейтингов");
            }
        } else {
            log.info("Проверка количества рейтингов MPA пройдена успешно (по памяти).");
            return;
        }
        log.info("Проверка количества рейтингов MPA пройдена успешно (по БД).");
    }


    public List<FilmDto> getFilmsByDirectorSorted(int directorId, String sortBy) {
        log.info("Получение фильмов режиссера {} с сортировкой {}", directorId, sortBy);
        checkDirectorById(directorId);

        List<Film> films = filmStorage.getFilmsByDirector(directorId);

        if (films.isEmpty()) {
            log.info("Не найдено фильмов для режиссера {} с сортировкой {}", directorId, sortBy);
            return Collections.emptyList();
        }

        if ("year".equalsIgnoreCase(sortBy)) {
            films.sort(Comparator.comparing(Film::getReleaseDate));
        } else if ("likes".equalsIgnoreCase(sortBy)) {
            films.sort((f1, f2) -> {
                int likes2 = filmStorage.countLikes(f2.getId());
                int likes1 = filmStorage.countLikes(f1.getId());
                return Integer.compare(likes2, likes1);
            });
        } else {
            throw new IllegalArgumentException("Неподдерживаемый параметр сортировки: " + sortBy);
        }
        log.info("Возвращено {} фильмов для режиссера {} отсортированных по {}", films.size(), directorId, sortBy);

        return mapFilmListToDto(films);
    }

    public List<FilmDto> searchFilms(String query, List<String> by) {
        log.info("Поиск фильмов по запросу '{}' с параметрами by={}", query, by);

        boolean searchByTitle = by != null && by.contains("title");
        boolean searchByDirector = by != null && by.contains("director");

        if (!searchByTitle && !searchByDirector) {
            throw new ValidationException("Параметр 'by' должен содержать 'title' и/или 'director'");
        }

        List<Film> films = filmStorage.searchFilms(query.toLowerCase(), searchByTitle, searchByDirector);

        return mapFilmListToDto(films);
    }

    public void removeFilm(int filmId) {
        log.info("Удаление фильма с id: {}", filmId);
        getFilmById(filmId);
        filmStorage.deleteFilm(filmId);
        log.info("Фильм с id = {} удален", filmId);
    }


    public Set<Integer> getFilmLikesByUserId(Integer userId) {
        log.debug("Получение списка лайков для пользователя с ID {} (делегирование в storage)", userId);
        return filmStorage.findFilmLikesByUserId(userId);
    }

    public Map<Integer, Set<Integer>> getAllUsersLikes() {
        log.debug("Получение всех лайков всех пользователей (делегирование в storage)");
        return filmStorage.findAllUsersLikes();
    }

    public List<FilmDto> getFilmsByIds(List<Integer> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) {
            log.debug("Список ID фильмов для получения пуст");
            return Collections.emptyList();
        }
        log.debug("Получение фильмов по списку ID: {} (deleting from storage and mapping)", filmIds);
        List<Film> films = filmStorage.findFilmsByIds(filmIds);

        return mapFilmListToDto(films);
    }
}