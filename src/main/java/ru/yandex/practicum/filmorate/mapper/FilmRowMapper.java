package ru.yandex.practicum.filmorate.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@RequiredArgsConstructor
@Component
public class FilmRowMapper implements RowMapper<Film> {
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getInt("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setDuration(rs.getInt("duration"));
        film.setReleaseDate(rs.getDate("release_dt").toLocalDate());


        int mpaId = rs.getInt("mpa_id");
        Mpa mpa = mpaStorage.findMpaById(mpaId)
                .orElseThrow(() -> new NotFoundException("MPA рейтинг не найден по id: " + mpaId));
        film.setMpa(mpa);

        List<Integer> genres = genreStorage.getByFilmId(rs.getInt("id")).stream()
                .map(Genre::getId)
                .toList();
        film.setGenres(genres);

        return film;
    }
}