package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Component
public interface GenreStorage {

    Optional<Genre> findGenreById(int genreId);

    List<Genre> getByFilmId(int filmId);

    List<Genre> findAllGenre();

    boolean checkGenreCount(Integer count);

}
