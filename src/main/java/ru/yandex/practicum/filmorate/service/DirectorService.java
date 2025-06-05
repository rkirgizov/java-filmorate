package ru.yandex.practicum.filmorate.service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.DirectorMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.List;

@Slf4j
@Service
public class DirectorService {
    @Autowired
    private final DirectorStorage directorStorage;

    public DirectorService(DirectorStorage directorStorage) {
        this.directorStorage = directorStorage;
    }

    public Director createDirector(Director director) {
        if (director.getName() == null || director.getName().isBlank()) {
            throw new ValidationException("Имя режиссёра не может быть пустым");
        }

        return directorStorage.createDirector(director);
    }

    public Director updateDirector(Director director) {
        DirectorDto existingDirector = findDirectorById(director.getId());

        if (existingDirector == null) {
            throw new NotFoundException("Режиссёр с id = " + director.getId() + " не найден");
        }

        return directorStorage.updateDirector(director);
    }

    public void deleteDirector(int id) {
        directorStorage.deleteDirector(id);
    }

    public DirectorDto findDirectorById(Integer directorId) {
        return directorStorage.findDirectorById(directorId)
                .map(DirectorMapper::mapToDirectorDto)
                .orElseThrow(() -> new NotFoundException(String.format("Жанр с id: %s не найден", directorId)));
    }

    public List<DirectorDto> findAll() {
        return directorStorage.findAllDirector().stream()
                .map(DirectorMapper::mapToDirectorDto)
                .toList();
    }

}
