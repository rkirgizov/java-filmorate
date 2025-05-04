package ru.yandex.practicum.filmorate.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.filmorate.enumeration.StatusFriendship;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(exclude = {"id", "name"})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    Long id;
    String email;
    String login;
    String name;
    LocalDate birthday;
    Set<Long> friends = new HashSet<>();
    StatusFriendship statusFriendship;
}