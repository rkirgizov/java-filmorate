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
        log.info("Получен запрос POST /directors для создания режиссера: {}", directorDto);
        return directorService.createDirector(directorDto);
    }

    @PutMapping
    public DirectorDto updateDirector(@RequestBody DirectorDto directorDto) {
        log.info("Получен запрос PUT /directors для обновления режиссера с id={}", directorDto.getId());
        return directorService.updateDirector(directorDto);
    }

    @DeleteMapping("/{id}")
    public void deleteDirector(@PathVariable int id) {
        log.info("Получен запрос DELETE /directors/{} для удаления режиссера", id);
        directorService.deleteDirector(id);
    }

    @GetMapping("/{id}")
    public DirectorDto findDirectorById(@PathVariable int id) {
        log.info("Получен запрос GET /directors/{} для поиска режиссера", id);
        return directorService.findDirectorById(id);
    }

    @GetMapping
    public List<DirectorDto> findAll() {
        log.info("Получен запрос GET /directors для получения всех режиссеров");
        return directorService.findAll();
    }
}
