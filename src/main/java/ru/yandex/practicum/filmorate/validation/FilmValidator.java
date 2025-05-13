package ru.yandex.practicum.filmorate.validation;

import lombok.Data;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
public final class FilmValidator {
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

}
