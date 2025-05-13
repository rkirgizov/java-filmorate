package ru.yandex.practicum.filmorate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Data
@AllArgsConstructor
public class FilmRequest {
    private String name;
    private String description;
    private Integer duration;
    private LocalDate releaseDate;
    private Mpa mpa;
    private List<Genre> genres;

    public boolean hasName() {
        return ! (name == null || name.isBlank());
    }

    public boolean hasDescription() {
        return ! (description == null || description.isBlank());
    }

    public boolean hasDuration() {
        return ! (duration == null);
    }

    public boolean hasReleaseDate() {
        return ! (releaseDate == null);
    }

    public boolean hasMpa() {
        return ! (mpa == null);
    }

    public boolean hasGenre() {
        return Objects.nonNull(genres) && !genres.isEmpty();
    }

}