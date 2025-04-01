package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(of = { "name" })
//@FieldDefaults(level = AccessLevel.PRIVATE)
public class Film {
    Long id;
    @NotNull
    @NotBlank
    String name;
    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    String description;
    @Past
    LocalDate releaseDate;
    @NotNull
    @Positive
    Integer duration;
}