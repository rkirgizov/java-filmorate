package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.BaseStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component("FilmStorageDbImpl")
public class FilmStorageDbImpl extends BaseStorage<Film> implements FilmStorage {
    private static final String FIND_BY_ID_QUERY = " SELECT * FROM _film WHERE id = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM _film";
    private static final String INSERT_QUERY = "INSERT INTO _film (name,description,duration,release_dt,mpa_id) VALUES (?,?,?,?,?)";
    private static final String INSERT_FILM_GENRE_QUERY = "INSERT INTO _film_genre (film_id, genre_id) VALUES (?,?)";
    private static final String CLEAR_FILM_GENRE_QUERY = "DELETE FROM _film_genre WHERE film_id = ?";
    private static final String UPDATE_QUERY = "UPDATE _film SET name = ?, description = ?, duration = ?, release_dt = ?, mpa_id = ? WHERE id = ?";
    private static final String INSERT_LIKE_QUERY = "INSERT INTO _like (user_id, film_id) VALUES (?,?)";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM _like WHERE user_id = ? AND film_id = ?";


    private static final String GET_FILMS_BY_DIRECTOR_SQL = " SELECT f.* FROM _film f JOIN _film_director fd ON f.id = fd.film_id WHERE fd.director_id = ? ";
    private static final String COUNT_LIKES_SQL = "SELECT COUNT(*) FROM _like WHERE film_id = ?";
    private static final String FIND_COMMON_FILMS_QUERY = """
                SELECT f.*
                FROM _film f
                JOIN _like l1 ON f.id = l1.film_id AND l1.user_id = ?
                JOIN _like l2 ON f.id = l2.film_id AND l2.user_id = ?
                LEFT JOIN _like all_likes ON f.id = all_likes.film_id
                GROUP BY f.id
                ORDER BY COUNT(all_likes.user_id) DESC
            """;

    private static final String DELETE_DIRECTORS_FROM_FILM = "DELETE FROM _film_director WHERE film_id = ?";
    private static final String INSERT_DIRECTOR_TO_FILM = "INSERT INTO _film_director (film_id, director_id) VALUES (?, ?)";

    private static final String BASE_POPULAR_QUERY = """
            SELECT f.*
            FROM _film f
            LEFT JOIN _like l ON f.id = l.film_id
            """;

    private static final String DELETE_FILM_QUERY = "DELETE FROM _film WHERE id = ?";
    private static final String DELETE_FILM_GENRES_QUERY = "DELETE FROM _film_genre WHERE film_id = ?";
    private static final String DELETE_FILM_LIKES_QUERY = "DELETE FROM _like WHERE film_id = ?";
    private static final String DELETE_FILM_DIRECTORS_QUERY = "DELETE FROM _film_director WHERE film_id = ?";

    private static final String FIND_FILM_LIKES_BY_USER_ID_SQL = "SELECT film_id FROM _like WHERE user_id = ?";
    private static final String FIND_ALL_USERS_LIKES_SQL = "SELECT user_id, film_id FROM _like";

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    @Autowired
    public FilmStorageDbImpl(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbc);
    }

    @Override
    public Optional<Film> findFilmById(int filmId) {
        log.debug("Поиск фильма в БД по id: {}", filmId);
        return findOne(FIND_BY_ID_QUERY, filmId);
    }

    @Override
    public List<Film> findAllFilms() {
        log.debug("Получение всех фильмов из БД");
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Film createFilm(Film film) {
        log.debug("Попытка создать фильм в БД: {}", film);
        int filmId = insert(
                INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                film.getDuration(),
                film.getReleaseDate(),
                film.getMpa().getId());
        film.setId(filmId);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            film.getGenres().forEach(genreId -> {
                        insert(INSERT_FILM_GENRE_QUERY, filmId, genreId);
                    }
            );
        }
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            film.getDirectors().forEach(directorId -> {
                insert(INSERT_DIRECTOR_TO_FILM, filmId, directorId);
            });
        }
        log.info("Фильм с id = {} - добавлен в БД", film.getId());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        log.debug("Попытка обновить фильм в БД с id: {}", film.getId());
        int filmId = film.getId();
        update(
                UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                film.getDuration(),
                film.getReleaseDate(),
                film.getMpa().getId(),
                filmId);

        delete(CLEAR_FILM_GENRE_QUERY, filmId);
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            film.getGenres().forEach(genreId -> {
                        insert(INSERT_FILM_GENRE_QUERY, filmId, genreId);
                    }
            );
        }

        delete(DELETE_DIRECTORS_FROM_FILM, filmId);
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            film.getDirectors().forEach(directorId -> {
                insert(INSERT_DIRECTOR_TO_FILM, filmId, directorId);
            });
        }


        log.info("Фильм с id = {} - обновлён в БД", film.getId());
        return film;
    }

    @Override
    public void addLikeToFilm(Integer filmId, Integer userId) {
        log.debug("Пользователь {} ставит лайк фильму {}", userId, filmId);
        try {
            insert(INSERT_LIKE_QUERY, userId, filmId);
            log.info("Лайк пользователя {} добавлен к фильму {}", userId, filmId);
        } catch (Exception e) {
            log.warn("Ошибка при добавлении лайка пользователем {} фильму {}: {}", userId, filmId, e.getMessage());
        }
    }

    @Override
    public void removeLikeFromFilm(Integer filmId, Integer userId) {
        log.debug("Пользователь {} удаляет лайк с фильма {}", userId, filmId);
        int affectedRows = jdbc.update(DELETE_LIKE_QUERY, userId, filmId);
        if (affectedRows > 0) {
            log.info("Лайк пользователя {} удален с фильма {}", userId, filmId);
        } else {
            log.warn("Лайк пользователя {} для фильма {} не найден для удаления", userId, filmId);
        }
    }

    @Override
    public List<Film> getPopularFilms(int count, Integer genreId, Integer year) {
        log.debug("Получение {} самых популярных фильмов из БД, фильтрация: genreId={}, year={}", count, genreId, year);

        StringBuilder sqlBuilder = new StringBuilder(BASE_POPULAR_QUERY);
        List<Object> params = new ArrayList<>();
        List<String> whereClauses = new ArrayList<>();

        if (genreId != null) {
            sqlBuilder.append(" JOIN _film_genre fg ON f.id = fg.film_id");
            whereClauses.add("fg.genre_id = ?");
            params.add(genreId);
        }

        if (year != null) {
            whereClauses.add("EXTRACT(YEAR FROM f.release_dt) = ?");
            params.add(year);
        }

        if (!whereClauses.isEmpty()) {
            sqlBuilder.append(" WHERE ").append(String.join(" AND ", whereClauses));
        }

        sqlBuilder.append(" GROUP BY f.id");

        sqlBuilder.append(" ORDER BY COUNT(l.user_id) DESC");

        sqlBuilder.append(" LIMIT ?");
        params.add(count);

        String finalSql = sqlBuilder.toString();
        log.debug("Executing popular films query: {}", finalSql);
        log.debug("With parameters: {}", params);

        return findMany(finalSql, params.toArray());
    }


    @Override
    public List<Film> getFilmsByDirector(int directorId) {
        log.debug("Получение фильмов режиссера {} из БД", directorId);
        return findMany(GET_FILMS_BY_DIRECTOR_SQL, directorId);
    }

    @Override
    public int countLikes(int filmId) {
        log.debug("Подсчет лайков для фильма с id {}", filmId);
        Integer likeCount = jdbc.queryForObject(COUNT_LIKES_SQL, Integer.class, filmId);
        return likeCount != null ? likeCount : 0;
    }

    @Override
    public List<Film> findCommonFilms(int userId, int friendId) {
        log.debug("Поиск общих фильмов в БД для пользователей {} и {}", userId, friendId);
        List<Film> commonFilms = findMany(FIND_COMMON_FILMS_QUERY, userId, friendId);
        log.info("Найдено {} общих фильмов для пользователей {} и {}", commonFilms.size(), userId, friendId);
        return commonFilms;
    }

    @Override
    public void deleteDirectorsFromFilm(int filmId) {
        log.debug("Удаление всех режиссеров для фильма с id {} в БД", filmId);
        jdbc.update(DELETE_DIRECTORS_FROM_FILM, filmId);
        log.debug("Удалено {} режиссеров для фильма с id {}", jdbc.update(DELETE_DIRECTORS_FROM_FILM, filmId), filmId);
    }

    @Override
    public void addDirectorToFilm(int filmId, int directorId) {
        log.debug("Добавление режиссера {} к фильму {} в БД", directorId, filmId);
        insert(INSERT_DIRECTOR_TO_FILM, filmId, directorId);
        log.debug("Режиссер {} добавлен к фильму {}", directorId, filmId);
    }

    @Override
    public List<Film> searchFilms(String query, boolean searchByTitle, boolean searchByDirector) {
        log.debug("Поиск фильмов в БД по запросу '{}' с параметрами by={}", query, List.of(searchByTitle ? "title" : "", searchByDirector ? "director" : "").stream().filter(s -> !s.isEmpty()).collect(Collectors.joining(",")));
        StringBuilder sqlBuilder = new StringBuilder(
                "SELECT f.*, COUNT(l.user_id) AS likes_count FROM _film f ");

        if (searchByDirector) {
            sqlBuilder.append("LEFT JOIN _film_director fd ON f.id = fd.film_id ");
            sqlBuilder.append("LEFT JOIN _director d ON fd.director_id = d.id ");
        }

        sqlBuilder.append("LEFT JOIN _like l ON f.id = l.film_id ");
        sqlBuilder.append("WHERE ");

        List<String> conditions = new ArrayList<>();
        MapSqlParameterSource parameters = new MapSqlParameterSource();

        if (searchByTitle) {
            conditions.add("LOWER(f.name) LIKE :query");
            parameters.addValue("query", "%" + query.toLowerCase() + "%");
        }

        if (searchByDirector) {
            if (!conditions.isEmpty()) sqlBuilder.append("(");
            conditions.add("LOWER(d.name) LIKE :query");
            parameters.addValue("query", "%" + query.toLowerCase() + "%");
            if (!conditions.isEmpty() && searchByTitle) sqlBuilder.append(")");
        }

        sqlBuilder.append(String.join(" OR ", conditions));
        sqlBuilder.append(" GROUP BY f.id ORDER BY likes_count DESC");

        log.debug("Executing search films query: {}", sqlBuilder.toString());
        log.debug("With parameters: {}", parameters.getValues());

        return namedParameterJdbcTemplate.query(sqlBuilder.toString(), parameters, getMapper());
    }

    @Override
    public void deleteFilm(int filmId) {
        log.info("Удаление фильма с id: {}", filmId);
        jdbc.update(DELETE_FILM_GENRES_QUERY, filmId);
        jdbc.update(DELETE_FILM_LIKES_QUERY, filmId);
        jdbc.update(DELETE_FILM_DIRECTORS_QUERY, filmId);

        int affectedRows = jdbc.update(DELETE_FILM_QUERY, filmId);

        if (affectedRows > 0) {
            log.debug("Фильм с id = {} удален", filmId);
        } else {
            log.warn("Фильм с id = {} не найден для удаления", filmId);
        }
    }

    @Override
    public Set<Integer> findFilmLikesByUserId(Integer userId) {
        log.debug("Поиск лайков для пользователя {} в БД", userId);
        List<Integer> filmIds = jdbc.queryForList(FIND_FILM_LIKES_BY_USER_ID_SQL, Integer.class, userId);
        log.debug("Найдено {} лайков для пользователя {}", filmIds.size(), userId);
        return new HashSet<>(filmIds);
    }

    @Override
    public Map<Integer, Set<Integer>> findAllUsersLikes() {
        log.debug("Поиск всех лайков всех пользователей в БД");
        List<Map<String, Object>> likesData = jdbc.queryForList(FIND_ALL_USERS_LIKES_SQL);

        Map<Integer, Set<Integer>> usersLikes = new HashMap<>();
        for (Map<String, Object> row : likesData) {
            Integer userId = (Integer) row.get("user_id");
            Integer filmId = (Integer) row.get("film_id");

            usersLikes.computeIfAbsent(userId, k -> new HashSet<>()).add(filmId);
        }
        log.debug("Собрана карта лайков для {} пользователей", usersLikes.size());
        return usersLikes;
    }

    @Override
    public List<Film> findFilmsByIds(List<Integer> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) {
            log.debug("Список ID фильмов для поиска пуст");
            return Collections.emptyList();
        }
        log.debug("Поиск фильмов в БД по списку ID: {}", filmIds);

        String sql = "SELECT * FROM _film WHERE id IN (:filmIds)";

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("filmIds", filmIds);

        List<Film> films = namedParameterJdbcTemplate.query(sql, parameters, getMapper());

        log.debug("Найдено {} фильмов по списку ID", films.size());
        return films;
    }
}