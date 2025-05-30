package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.BaseStorage;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
public class DirectorStorageDbImpl extends BaseStorage<Director> implements DirectorStorage {
    private static final String INSERT_DIRECTOR_QUERY = "INSERT INTO _director (name) VALUES (?)";
    private static final String UPDATE_DIRECTOR_QUERY = "UPDATE _director SET name = ? WHERE id = ?";
    private static final String DELETE_DIRECTOR_QUERY = "DELETE FROM _director WHERE id = ?";
    private static final String FIND_BY_ID_QUERY = " SELECT * FROM _director WHERE id = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM _director";
    private static final String FIND_ALL_BY_FILM_ID = "SELECT d.* FROM _director d " +
            "JOIN _film_director fd ON d.id = fd.director_id " +
            "WHERE fd.film_id = ?";
    private static final String COUNT_QUERY = "SELECT COUNT(*) FROM _director";

    public DirectorStorageDbImpl(JdbcTemplate jdbc, RowMapper<Director> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Director createDirector(Director director) {
        int directorId = insert(INSERT_DIRECTOR_QUERY, director.getName());
        director.setId(directorId);
        log.debug("Режиссёр {} добавлен с id={}", director.getName(), directorId);
        return director;
    }

    @Override
    public Director updateDirector(Director director) {
        update(UPDATE_DIRECTOR_QUERY, director.getName(), director.getId());
        log.debug("Режиссёр с id={} обновлён", director.getId());
        return director;
    }

    @Override
    public void deleteDirector(int id) {
        update(DELETE_DIRECTOR_QUERY, id);
        log.debug("Режиссёр с id={} удален", id);
    }

    @Override
    public Optional<Director> findDirectorById(int directorId) {
        return findOne(FIND_BY_ID_QUERY, directorId);
    }

    @Override
    public List<Director> findAllDirector() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public List<Director> getByFilmId(int filmId) {
        return findMany(FIND_ALL_BY_FILM_ID, filmId);
    }

    @Override
    public boolean checkDirectorCount(Integer needCount) {
        Integer count = count(COUNT_QUERY);
        return (Objects.equals(count, needCount));
    }

}












