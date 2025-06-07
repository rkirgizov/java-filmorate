package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorStorage;

    public DirectorDto createDirector(DirectorDto directorDto) {
        log.info("Создание режиссёра: {}", directorDto);
        if (directorDto.getName() == null || directorDto.getName().isBlank()) {
            log.warn("Попытка создать режиссёра с пустым именем");
            throw new ValidationException("Имя режиссёра не может быть пустым");
        }

        Director director = new Director();
        director.setName(directorDto.getName());
        Director createdDirector = directorStorage.createDirector(director);
        DirectorDto result = DirectorMapper.mapToDirectorDto(createdDirector);
        log.info("Режиссёр успешно создан: {}", result);
        return result;
    }

    public DirectorDto updateDirector(DirectorDto directorDto) {
        log.info("Обновление режиссёра: {}", directorDto);
        if (directorDto.getId() == null) {
            log.warn("Попытка обновить режиссёра без указания id");
            throw new ValidationException("ID режиссёра должен быть указан");
        }
        if (directorDto.getName() == null || directorDto.getName().isBlank()) {
            log.warn("Попытка обновить режиссёра с пустым именем");
            throw new ValidationException("Имя режиссёра не может быть пустым");
        }

        Director director = new Director();
        director.setId(directorDto.getId());
        director.setName(directorDto.getName());
        Director updatedDirector = directorStorage.updateDirector(director);
        DirectorDto result = DirectorMapper.mapToDirectorDto(updatedDirector);
        log.info("Режиссёр успешно обновлён: {}", result);
        return result;
    }


    public void deleteDirector(int id) {
        log.info("Удаление режиссёра с id: {}", id);
        directorStorage.deleteDirector(id);
        log.info("Режиссёр с id={} успешно удалён", id);
    }

    public DirectorDto findDirectorById(Integer directorId) {
        log.info("Поиск режиссёра по id: {}", directorId);
        DirectorDto director = directorStorage.findDirectorById(directorId)
                .map(DirectorMapper::mapToDirectorDto)
                .orElseThrow(() -> {
                    log.warn("Режиссёр с id={} не найден", directorId);
                    return new NotFoundException(String.format("Режиссёр с id: %s не найден", directorId));
                });
        log.info("Режиссёр найден: {}", director);
        return director;
    }

    public List<DirectorDto> findAll() {
        log.info("Получение всех режиссёров");
        List<DirectorDto> directors = directorStorage.findAllDirector().stream()
                .map(DirectorMapper::mapToDirectorDto)
                .toList();
        log.info("Возвращено {} режиссёров", directors.size());
        return directors;
    }
}