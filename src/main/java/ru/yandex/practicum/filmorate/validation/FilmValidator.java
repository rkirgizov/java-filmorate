package ru.yandex.practicum.filmorate.validation;

import lombok.extern.slf4j.Slf4j;
import lombok.Data;
import ru.yandex.practicum.filmorate.dto.FilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Data
public final class FilmValidator {

    public static FilmRequest validateFilmRequestNew(FilmRequest filmRequest, MpaStorage mpaStorage, GenreStorage genreStorage, DirectorStorage directorStorage) {
        if (hasNoName(filmRequest)) {
            throw new ValidationException("Название фильма не заполнено");
        }

        if (hasNoDescription(filmRequest)) {
            throw new ValidationException("Описание фильма не заполнено");
        }
        validateDescription(filmRequest.getDescription());

        if (hasNoReleaseDate(filmRequest)) {
            throw new ValidationException("Дата релиза фильма не заполнена");
        }
        validateReleaseDate(filmRequest.getReleaseDate());

        if (hasNoDuration(filmRequest)) {
            throw new ValidationException("Продолжительность фильма не заполнена");
        }
        validateDuration(filmRequest.getDuration());

        if (hasNoMpa(filmRequest)) {
            log.warn("Для добавляемого фильма {} не указан возрастной рейтинг", filmRequest.getName());
            filmRequest.setMpa(null);
        } else {
            filmRequest.setMpa(validateMpa(filmRequest.getMpa().getId(), mpaStorage));
        }

        if (hasNoGenre(filmRequest)) {
            log.warn("Для добавляемого фильма {} не указан ни один жанр", filmRequest.getName());
            filmRequest.setGenres(new ArrayList<>());
        } else {
            filmRequest.setGenres(validateGenre(filmRequest.getGenres(), genreStorage));
        }

        if (hasNoDirector(filmRequest)) {
            log.warn("Для фильма {} не указан ни один режиссёр", filmRequest.getName());
            filmRequest.setDirectors(new ArrayList<>());
        } else {
            filmRequest.setDirectors(validateDirectors(filmRequest.getDirectors(), directorStorage));
        }

        return filmRequest;
    }

    public static FilmRequest validateFilmRequestForUpdate(Film film, FilmRequest filmRequest, MpaStorage mpaStorage, GenreStorage genreStorage, DirectorStorage directorStorage) {
        if (hasNoName(filmRequest)) {
            filmRequest.setName(film.getName());
        }

        if (hasNoDescription(filmRequest)) {
            filmRequest.setDescription(film.getDescription());
        } else {
            validateDescription(filmRequest.getDescription());
        }

        if (hasNoReleaseDate(filmRequest)) {
            filmRequest.setReleaseDate(film.getReleaseDate());
        } else {
            validateReleaseDate(filmRequest.getReleaseDate());
        }

        if (hasNoDuration(filmRequest)) {
            filmRequest.setDuration(film.getDuration());
        } else {
            validateDuration(filmRequest.getDuration());
        }

        if (hasNoMpa(filmRequest)) {
            filmRequest.setMpa(film.getMpa());
        } else {
            validateMpa(filmRequest.getMpa().getId(), mpaStorage);
        }

        if (hasNoGenre(filmRequest)) {
            List<Genre> genres = film.getGenres().stream()
                    .map(genreStorage::findGenreById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();
            filmRequest.setGenres(genres);
        } else {
            validateGenre(filmRequest.getGenres(), genreStorage);
        }

        if (hasNoDirector(filmRequest)) {
            List<Director> directors = film.getDirectors().stream()
                    .map(directorStorage::findDirectorById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();
            filmRequest.setDirectors(directors);
        } else {
            validateDirectors(filmRequest.getDirectors(), directorStorage);
        }

        return filmRequest;
    }

    /**
     * Проверка наличия параметров в реквесте
     */

    public static boolean hasNoName(FilmRequest request) {
        return request.getName() == null || request.getName().isBlank();
    }

    public static boolean hasNoDescription(FilmRequest request) {
        return request.getDescription() == null || request.getDescription().isBlank();
    }

    public static boolean hasNoDuration(FilmRequest request) {
        return request.getDuration() == null;
    }

    public static boolean hasNoReleaseDate(FilmRequest request) {
        return request.getReleaseDate() == null;
    }

    public static boolean hasNoMpa(FilmRequest request) {
        return request.getMpa() == null;
    }

    public static boolean hasNoGenre(FilmRequest request) {
        return !Objects.nonNull(request.getGenres()) || request.getGenres().isEmpty();
    }

    public static boolean hasNoDirector(FilmRequest request) {
        return request.getDirectors() == null || request.getDirectors().isEmpty();
    }
    /**
     * Валидация параметров фильма
     */

    public static void validateDescription(String description) {
        if (description.length() >= 200) {
            throw new ValidationException("Описание фильма не должно превышать 200 символов");
        }
    }

    public static void validateReleaseDate(LocalDate birthday) {
        if (birthday.isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }

    public static void validateDuration(int duration) {
        if (duration <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }

    public static Mpa validateMpa(int mpaId, MpaStorage mpaStorage) {
        return mpaStorage.findMpaById(mpaId)
                .orElseThrow(() -> new NotFoundException(String.format("Рейтинг МПА с id = %d не найден в справочнике", mpaId)));
    }

    public static List<Genre> validateGenre(List<Genre> genres, GenreStorage genreStorage) {
        return genres.stream()
                .map(Genre::getId)
                .distinct() // убираем дубликаты
                .map(genreId -> genreStorage.findGenreById(genreId)
                        .orElseThrow(() -> new NotFoundException(String.format("Жанр с id = %d не найден в справочнике", genreId))))
                .collect(Collectors.toList());
    }



    public static List<Director> validateDirectors(List<Director> directors, DirectorStorage directorStorage) {
        return directors.stream()
                .map(d -> directorStorage.findDirectorById(d.getId())
                        .orElseThrow(() -> new NotFoundException("Режиссёр с id = " + d.getId() + " не найден")))
                .toList();
    }

}
