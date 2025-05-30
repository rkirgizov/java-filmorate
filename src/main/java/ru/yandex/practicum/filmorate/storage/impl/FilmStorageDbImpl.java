package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.BaseStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.List;
import java.util.Optional;

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
    private static final String FIND_POPULAR_QUERY = """
                SELECT f.*
                FROM _film f
                LEFT JOIN _like l ON f.id = l.film_id
                GROUP BY f.id
                HAVING COUNT(l.user_id) > 0
                ORDER BY COUNT(l.user_id) DESC
                LIMIT ?
            """;
    private static final String GET_FILMS_BY_DIRECTOR_SQL = " SELECT f.* FROM _film f JOIN _film_director fd ON f.id = fd.film_id WHERE fd.director_id = ? ";
    private static final String COUNT_LIKES_SQL = "SELECT COUNT(*) FROM _like WHERE film_id = ?";

    public FilmStorageDbImpl(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Optional<Film> findFilmById(int filmId) {
        return findOne(FIND_BY_ID_QUERY, filmId);
    }

    @Override
    public List<Film> findAllFilms() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Film createFilm(Film film) {
        int filmId = insert(
                INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                film.getDuration(),
                film.getReleaseDate(),
                film.getMpa().getId());
        film.setId(filmId);
        film.getGenres().forEach(genreId -> {
                    insert(INSERT_FILM_GENRE_QUERY, filmId, genreId);
                }
        );
        log.debug("Фильм с id = {} - добавлен", film.getId());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        int filmId = film.getId();
        update(
                UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                film.getDuration(),
                film.getReleaseDate(),
                film.getMpa().getId(),
                film.getDirectors(),
                filmId);
        delete(CLEAR_FILM_GENRE_QUERY, filmId);
        film.getGenres().forEach(genreId -> {
                    insert(INSERT_FILM_GENRE_QUERY, filmId, genreId);
                }
        );
        log.debug("Фильм с id = {} - обновлён", film.getId());
        return film;
    }

    @Override
    public void addLikeToFilm(Integer filmId, Integer userId) {
        insert(INSERT_LIKE_QUERY, userId, filmId);
    }

    @Override
    public void removeLikeFromFilm(Integer filmId, Integer userId) {
        delete(DELETE_LIKE_QUERY, userId, filmId);
    }

    @Override
    public List<Film> getPopularFilms(Integer limit) {
        return findMany(FIND_POPULAR_QUERY, limit);
    }


    @Override
    public List<Film> getFilmsByDirector(int directorId) {
        return findMany(GET_FILMS_BY_DIRECTOR_SQL, directorId);
    }

    @Override
    public int countLikes(int filmId) {
        return jdbc.queryForObject(COUNT_LIKES_SQL, Integer.class, filmId);
    }
}
