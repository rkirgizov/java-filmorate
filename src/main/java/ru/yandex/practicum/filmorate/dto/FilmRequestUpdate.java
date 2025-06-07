package ru.yandex.practicum.filmorate.dto;

import lombok.Getter;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.List;

@Getter
public class FilmRequestUpdate extends FilmRequest {
    private final Integer id;

    public FilmRequestUpdate(String name, String description, Integer duration, LocalDate releaseDate, Mpa mpa, List<Genre> genres, Integer id, List<Director> directors) {
        super(name, description, duration, releaseDate, mpa, genres, directors);
        this.id = id;
    }

}
