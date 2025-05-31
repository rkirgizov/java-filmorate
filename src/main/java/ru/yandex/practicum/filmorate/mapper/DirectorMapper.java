package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.model.Director;

public class DirectorMapper {
    public static DirectorDto mapToDirectorDto(Director genre) {
        DirectorDto dto = new DirectorDto();
        dto.setId(genre.getId());
        dto.setName(genre.getName());

        return dto;
    }
}
