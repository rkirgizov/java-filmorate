package ru.yandex.practicum.filmorate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class UserRequest {
    private String login;
    private String email;
    private String name;
    private LocalDate birthday;

}