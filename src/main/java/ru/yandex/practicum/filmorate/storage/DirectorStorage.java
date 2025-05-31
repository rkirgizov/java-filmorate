package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Optional;

@Component
public interface DirectorStorage {

    Director createDirector(Director director);

    Director updateDirector(Director director);

    void deleteDirector(int id);

    Optional<Director> findDirectorById(int directorId);

    List<Director> getByFilmId(int filmId);

    List<Director> findAllDirector();

    boolean checkDirectorCount(Integer count);

}