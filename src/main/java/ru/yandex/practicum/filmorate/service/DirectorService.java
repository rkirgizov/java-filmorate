package ru.yandex.practicum.filmorate.service;
import lombok.extern.slf4j.Slf4j;
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

    private final DirectorStorage directorStorage;

    public DirectorService(DirectorStorage directorStorage) {
        this.directorStorage = directorStorage;
    }

    public DirectorDto createDirector(DirectorDto directorDto) {
        if (directorDto.getName() == null || directorDto.getName().isBlank()) {
            throw new ValidationException("Имя режиссёра не может быть пустым");
        }
        log.info("Создание режиссера: {}", directorDto);
        return DirectorMapper.mapToDirectorDto(directorStorage.createDirector(DirectorMapper.mapToDirector(directorDto)));
    }

    public DirectorDto updateDirector(DirectorDto directorDto) {
        log.info("Обновление режиссера с id={}", directorDto.getId());
        Director director = DirectorMapper.mapToDirector(directorDto);
        directorStorage.updateDirector(director);
        return findDirectorById(directorDto.getId());
    }

    public void deleteDirector(int id) {
        directorStorage.deleteDirector(id);
    }

    public DirectorDto findDirectorById(Integer directorId) {
        return directorStorage.findDirectorById(directorId)
                .map(DirectorMapper::mapToDirectorDto)
                .orElseThrow(() -> new NotFoundException(String.format("Режиссер с id: %s не найден", directorId)));
    }

    public List<DirectorDto> findAll() {
        return directorStorage.findAllDirector().stream()
                .map(DirectorMapper::mapToDirectorDto)
                .toList();
    }

}
