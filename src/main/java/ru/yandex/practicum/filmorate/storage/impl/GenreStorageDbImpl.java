package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.BaseStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.*;

@Slf4j
@Component
public class GenreStorageDbImpl extends BaseStorage<Genre> implements GenreStorage {
    private static final String FIND_BY_ID_QUERY = " SELECT * FROM _genre WHERE id = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM _genre";
    private static final String FIND_ALL_BY_FILM_ID = "SELECT g.* FROM _genre g " +
            "JOIN _film_genre fg ON g.id = fg.genre_id " +
            "WHERE fg.film_id = ?";
    private static final String COUNT_QUERY = "SELECT COUNT(*) FROM _genre";

    private static final String FIND_GENRES_BY_IDS_SQL = "SELECT id, name FROM _genre WHERE id IN (:genreIds)";
    private static final String GET_GENRES_BY_FILM_IDS_SQL = "SELECT fg.film_id, g.id, g.name FROM _genre g JOIN _film_genre fg ON g.id = fg.genre_id WHERE fg.film_id IN (:filmIds)";


    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public GenreStorageDbImpl(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbc);
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

    @Override
    public List<Genre> findGenresByIds(List<Integer> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            log.debug("Список ID жанров для поиска пуст");
            return Collections.emptyList();
        }
        log.debug("Поиск жанров в БД по списку ID: {}", genreIds);

        String sql = FIND_GENRES_BY_IDS_SQL;
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("genreIds", genreIds);

        List<Genre> genres = namedParameterJdbcTemplate.query(sql, parameters, getMapper());
        log.debug("Найдено {} жанров по списку ID", genres.size());
        return genres;
    }

    @Override
    public Map<Integer, List<Genre>> getGenresMapByFilmIds(List<Integer> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) {
            log.debug("Список ID фильмов для получения жанров пуст");
            return new HashMap<>();
        }
        log.debug("Поиск жанров для фильмов в БД по списку ID фильмов: {}", filmIds);

        String sql = GET_GENRES_BY_FILM_IDS_SQL;
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("filmIds", filmIds);

        List<Map<String, Object>> rows = namedParameterJdbcTemplate.queryForList(sql, parameters);

        Map<Integer, List<Genre>> genresMap = new HashMap<>();

        for (Map<String, Object> row : rows) {
            Integer filmId = (Integer) row.get("film_id");
            Genre currentGenre = new Genre();
            currentGenre.setId((Integer) row.get("id"));
            currentGenre.setName((String) row.get("name"));

            genresMap.computeIfAbsent(filmId, k -> new ArrayList<>()).add(currentGenre);
        }
        log.debug("Собрана карта жанров для {} фильмов", genresMap.size());
        return genresMap;
    }
}