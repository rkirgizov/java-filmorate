package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.BaseStorage;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.*;

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

    private static final String FIND_DIRECTORS_BY_IDS_SQL = "SELECT id, name FROM _director WHERE id IN (:directorIds)";
    private static final String GET_DIRECTORS_BY_FILM_IDS_SQL = "SELECT fd.film_id, d.id, d.name FROM _director d JOIN _film_director fd ON d.id = fd.director_id WHERE fd.film_id IN (:filmIds)";

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public DirectorStorageDbImpl(JdbcTemplate jdbc, RowMapper<Director> mapper) {
        super(jdbc, mapper);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbc);
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

    @Override
    public List<Director> findDirectorsByIds(List<Integer> directorIds) {
        if (directorIds == null || directorIds.isEmpty()) {
            log.debug("Список ID режиссеров для поиска пуст");
            return Collections.emptyList();
        }
        log.debug("Поиск режиссеров в БД по списку ID: {}", directorIds);

        String sql = FIND_DIRECTORS_BY_IDS_SQL;
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("directorIds", directorIds);

        List<Director> directors = namedParameterJdbcTemplate.query(sql, parameters, getMapper());
        log.debug("Найдено {} режиссеров по списку ID", directors.size());
        return directors;
    }

    @Override
    public Map<Integer, List<Director>> getDirectorsMapByFilmIds(List<Integer> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) {
            log.debug("Список ID фильмов для получения режиссеров пуст");
            return new HashMap<>();
        }
        log.debug("Поиск режиссеров для фильмов в БД по списку ID фильмов: {}", filmIds);

        String sql = GET_DIRECTORS_BY_FILM_IDS_SQL;
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("filmIds", filmIds);

        List<Map<String, Object>> rows = namedParameterJdbcTemplate.queryForList(sql, parameters);

        Map<Integer, List<Director>> directorsMap = new HashMap<>();

        for (Map<String, Object> row : rows) {
            Integer filmId = (Integer) row.get("film_id");
            Director currentDirector = new Director();
            currentDirector.setId((Integer) row.get("id"));
            currentDirector.setName((String) row.get("name"));

            directorsMap.computeIfAbsent(filmId, k -> new ArrayList<>()).add(currentDirector);
        }
        log.debug("Собрана карта режиссеров для {} фильмов", directorsMap.size());
        return directorsMap;
    }
}