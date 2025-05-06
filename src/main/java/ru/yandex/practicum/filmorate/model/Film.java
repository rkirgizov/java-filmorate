package ru.yandex.practicum.filmorate.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(exclude = {"id", "description"})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Film {
    Long id;
    Set<Long> genres = new HashSet<>();
    String rating;
    String name;
    String description;
    LocalDate releaseDate;
    Integer duration;
    Set<Long> likes = new HashSet<>();
}