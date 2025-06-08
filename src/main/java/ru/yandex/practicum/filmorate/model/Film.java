package ru.yandex.practicum.filmorate.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.*;

@Data
@EqualsAndHashCode(exclude = {"id", "description"})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Film {
    Integer id;
    String name;
    String description;
    Integer duration;
    LocalDate releaseDate;
    Mpa mpa;
    List<Integer> genres = new ArrayList<>();
    List<Integer> directors = new ArrayList<>();


}