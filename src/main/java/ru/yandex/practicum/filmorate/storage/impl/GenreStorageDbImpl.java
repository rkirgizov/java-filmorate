package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.BaseStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
public class GenreStorageDbImpl extends BaseStorage<Genre> implements GenreStorage {
    private static final String FIND_BY_ID_QUERY = " SELECT * FROM _genre WHERE id = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM _genre";
    private static final String FIND_ALL_BY_FILM_ID = "SELECT g.* FROM _genre g " +
            "JOIN _film_genre fg ON g.id = fg.genre_id " +
            "WHERE fg.film_id = ?";
    private static final String COUNT_QUERY = "SELECT COUNT(*) FROM _genre";

    public GenreStorageDbImpl(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Optional<Genre> findGenreById(int genreId) {
        return findOne(FIND_BY_ID_QUERY, genreId);
    }

    @Override
    public List<Genre> findAllGenre() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public List<Genre> getByFilmId(int filmId) {
        return findMany(FIND_ALL_BY_FILM_ID, filmId);
    }

    @Override
    public boolean checkGenreCount(Integer needCount) {
        Integer count = count(COUNT_QUERY);
        return (Objects.equals(count, needCount));
    }

}












