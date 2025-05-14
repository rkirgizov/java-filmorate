package ru.yandex.practicum.filmorate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class FilmRequest {
    private String name;
    private String description;
    private Integer duration;
    private LocalDate releaseDate;
    private Mpa mpa;
    private List<Genre> genres;
}