package ru.yandex.practicum.filmorate.controller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
@Slf4j
public class DirectorController {
    private final DirectorService directorService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DirectorDto createDirector(@RequestBody DirectorDto directorDto) {
        log.info("Получен запрос POST /directors для создания режиссёра: {}", directorDto);
        DirectorDto createdDirector = directorService.createDirector(directorDto);
        log.info("Режиссёр успешно создан с id: {}", createdDirector.getId());
        return createdDirector;
    }

    @PutMapping
    public DirectorDto updateDirector(@RequestBody DirectorDto directorDto) {
        log.info("Получен запрос PUT /directors для обновления режиссёра: {}", directorDto);
        DirectorDto updatedDirector = directorService.updateDirector(directorDto);
        log.info("Режиссёр с id={} успешно обновлён", directorDto.getId());
        return updatedDirector;
    }

    @DeleteMapping("/{id}")
    public void deleteDirector(@PathVariable int id) {
        log.info("Получен запрос DELETE /directors/{} для удаления режиссёра", id);
        directorService.deleteDirector(id);
        log.info("Режиссёр с id={} успешно удалён", id);
    }

    @GetMapping("/{id}")
    public DirectorDto findDirectorById(@PathVariable int id) {
        log.info("Получен запрос GET /directors/{} для поиска режиссёра", id);
        DirectorDto director = directorService.findDirectorById(id);
        log.info("Режиссёр с id={} успешно найден", id);
        return director;
    }

    @GetMapping
    public List<DirectorDto> findAll() {
        log.info("Получен запрос GET /directors для получения всех режиссёров");
        List<DirectorDto> directors = directorService.findAll();
        log.info("Возвращено {} режиссёров", directors.size());
        return directors;
    }
}
