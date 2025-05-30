package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.FilmRequest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FilmMapper {

    public static Film mapToFilm(FilmRequest request) {
        Film film = new Film();
        film.setName(request.getName());
        film.setDescription(request.getDescription());
        film.setDuration(request.getDuration());
        film.setReleaseDate(request.getReleaseDate());
        film.setMpa(request.getMpa());

        List<Integer> genres = request.getGenres().stream()
                .map(Genre::getId)
                .toList();
        film.setGenres(genres);

        List<Integer> directors = request.getDirectors().stream()
                .map(Director::getId)
                .toList();
        film.setDirectors(directors);

        return film;
    }

    public static FilmDto mapToFilmDto(Film film, MpaStorage mpaStorage, GenreStorage genreStorage, DirectorStorage directorStorage) {
        FilmDto dto = new FilmDto();
        dto.setId(film.getId());
        dto.setName(film.getName());
        dto.setDescription(film.getDescription());
        dto.setDuration(film.getDuration());
        dto.setReleaseDate(film.getReleaseDate());

        Optional<Mpa> mpa = mpaStorage.findMpaById(film.getMpa().getId());
        if (mpa.isPresent()) {
            dto.setMpa(mpa.get());
        } else {
            dto.setMpa(null);
        }

        List<Genre> genres = film.getGenres().stream()
                .map(genreId -> genreStorage.findGenreById(genreId).orElse(null))
                .filter(Objects::nonNull)
                .toList();
        dto.setGenres(genres);

        List<Director> directors = film.getDirectors().stream()
                .map(directorStorage::findDirectorById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        dto.setDirectors(directors);

        return dto;
    }



}