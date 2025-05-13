package ru.yandex.practicum.filmorate.dto;

import java.time.LocalDate;

public class UserRequestUpdate extends UserRequest {
    private final Integer id;

    public UserRequestUpdate(Integer id, String login, String email, String name, LocalDate birthday) {
        super(login, email, name, birthday);
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

}
