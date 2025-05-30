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
    private static final String FIND_COMMON_FILMS_QUERY = """
                SELECT f.*
                FROM _film f
                JOIN _like l1 ON f.id = l1.film_id AND l1.user_id = ? -- Фильмы, лайкнутые первым пользователем
                JOIN _like l2 ON f.id = l2.film_id AND l2.user_id = ? -- Фильмы, лайкнутые вторым пользователем
                LEFT JOIN _like all_likes ON f.id = all_likes.film_id -- Соединяем еще раз для подсчета общего количества лайков
                GROUP BY f.id -- Группируем по фильму
                ORDER BY COUNT(all_likes.user_id) DESC -- Сортируем по общему количеству лайков (популярности)
            """;

    public FilmStorageDbImpl(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
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
        delete(DELETE_LIKE_QUERY, userId, filmId);
        log.info("Лайк пользователя {} удален с фильма {}", userId, filmId);
    }

    @Override
    public List<Film> getPopularFilms(Integer limit) {
        log.debug("Получение {} самых популярных фильмов из БД", limit);
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
    @Override
    public List<Film> findCommonFilms(int userId, int friendId) {
        log.debug("Поиск общих фильмов в БД для пользователей {} и {}", userId, friendId);
        List<Film> commonFilms = findMany(FIND_COMMON_FILMS_QUERY, userId, friendId);
        log.info("Найдено {} общих фильмов для пользователей {} и {}", commonFilms.size(), userId, friendId);
        return commonFilms;
    }
}