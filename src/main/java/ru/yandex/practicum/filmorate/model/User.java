package ru.yandex.practicum.filmorate.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(exclude = {"id", "name"})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    Integer id;
    String login;
    String email;
    String name;
    LocalDate birthday;
}