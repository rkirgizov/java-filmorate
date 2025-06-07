package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component("FilmStorageInMemory")
@RequiredArgsConstructor
public class FilmStorageInMemory implements FilmStorage {

    private final Map<Integer, Film> films = new HashMap<>();
    private final Map<Integer, Set<Integer>> filmLikes = new HashMap<>();
    private final Map<Integer, Set<Integer>> filmDirectors = new HashMap<>();

    private final DirectorStorage directorStorage;

    private int nextId = 1;

    @Override
    public Optional<Film> findFilmById(int filmId) {
        log.debug("Поиск фильма в памяти по id: {}", filmId);
        return Optional.ofNullable(films.get(filmId));
    }

    @Override
    public Film createFilm(Film film) {
        log.debug("Попытка создать фильм в памяти: {}", film);
        film.setId(getNextId());
        films.put(film.getId(), film);

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            Set<Integer> directorIds = new HashSet<>(film.getDirectors());
            filmDirectors.put(film.getId(), directorIds);
            log.debug("Сохранены режиссеры для фильма {}: {}", film.getId(), directorIds);
        } else {
            filmDirectors.remove(film.getId());
        }

        log.info("Фильм с id = {} - добавлен в памяти", film.getId());
        return film;
    }

    @Override
    public Film updateFilm(Film updatedFilm) {
        log.debug("Попытка обновить фильм в памяти с id: {}", updatedFilm.getId());
        if (updatedFilm.getId() == null) {
            throw new ValidationException("Для обновления фильма необходимо указать id");
        }
        if (!films.containsKey(updatedFilm.getId())) {
            throw new NotFoundException(String.format("Фильм с id = %d - не найден для обновления в памяти", updatedFilm.getId()));
        }

        Film oldFilm = films.get(updatedFilm.getId());
        oldFilm.setName(updatedFilm.getName());
        oldFilm.setDescription(updatedFilm.getDescription());
        oldFilm.setReleaseDate(updatedFilm.getReleaseDate());
        oldFilm.setDuration(updatedFilm.getDuration());
        oldFilm.setMpa(updatedFilm.getMpa());
        oldFilm.setGenres(updatedFilm.getGenres());

        if (updatedFilm.getDirectors() != null) {
            if (updatedFilm.getDirectors().isEmpty()) {
                filmDirectors.remove(updatedFilm.getId());
                log.debug("Режиссеры удалены для фильма {}", updatedFilm.getId());
            } else {
                Set<Integer> directorIds = updatedFilm.getDirectors().stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                filmDirectors.put(updatedFilm.getId(), directorIds);
                log.debug("Обновлены режиссеры для фильма {}: {}", updatedFilm.getId(), directorIds);
            }
        } else {
            log.debug("Список режиссеров в запросе обновления для фильма {} отсутствует, связи не изменяются", updatedFilm.getId());
        }


        log.info("Фильм с id = {} - обновлён в памяти", updatedFilm.getId());
        return oldFilm;
    }

    @Override
    public void addLikeToFilm(Integer filmId, Integer userId) {
        log.debug("Пользователь {} ставит лайк фильму {} в памяти", userId, filmId);
        filmLikes.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
        log.info("Лайк пользователя {} добавлен к фильму {}", userId, filmId);
    }

    @Override
    public void removeLikeFromFilm(Integer filmId, Integer userId) {
        log.debug("Пользователь {} удаляет лайк с фильма {} в памяти", userId, filmId);
        Set<Integer> likes = filmLikes.get(filmId);
        if (likes != null) {
            boolean removed = likes.remove(userId);
            if (removed) {
                log.info("Лайк пользователя {} удален с фильма {}", userId, filmId);
            } else {
                log.warn("Попытка удалить лайк пользователя {} с фильма {} в памяти, но лайк не найден", userId, filmId);
            }
            if (likes.isEmpty()) {
                filmLikes.remove(filmId);
            }
        } else {
            log.warn("Попытка удалить лайк пользователя {} с фильма {} в памяти, но лайков для фильма не найдено", userId, filmId);
        }
    }

    @Override
    public List<Film> getPopularFilms(int count, Integer genreId, Integer year) {
        log.debug("Получение {} самых популярных фильмов из памяти, фильтрация: genreId={}, year={}", count, genreId, year);

        List<Film> filteredFilms = films.values().stream()
                .filter(film -> {
                    boolean matchesGenre = true;
                    if (genreId != null) {
                        matchesGenre = film.getGenres() != null && !film.getGenres().isEmpty() && film.getGenres().contains(genreId);
                    }

                    boolean matchesYear = true;
                    if (year != null) {
                        matchesYear = film.getReleaseDate() != null && film.getReleaseDate().getYear() == year;
                    }

                    return matchesGenre && matchesYear;
                })
                .collect(Collectors.toList());

        filteredFilms.sort((f1, f2) -> {
            int likes1 = filmLikes.getOrDefault(f1.getId(), Collections.emptySet()).size();
            int likes2 = filmLikes.getOrDefault(f2.getId(), Collections.emptySet()).size();
            return Integer.compare(likes2, likes1);
        });

        List<Film> result = filteredFilms.stream()
                .limit(count)
                .collect(Collectors.toList());

        log.info("Найдено {} популярных фильмов в памяти с фильтрацией", result.size());
        return result;
    }


    @Override
    public List<Film> findAllFilms() {
        log.debug("Получение всех фильмов из памяти");
        return new ArrayList<>(films.values());
    }

    private int getNextId() {
        while (films.containsKey(nextId)) {
            nextId++;
        }
        return nextId++;
    }

    @Override
    public List<Film> findCommonFilms(int userId, int friendId) {
        log.warn("In-memory FilmStorage does not fully support common films logic. Returning empty list.");
        return new ArrayList<>();
    }

    @Override
    public List<Film> getFilmsByDirector(int directorId) {
        log.debug("Получение фильмов режиссера {} из памяти", directorId);
        return films.values().stream()
                .filter(film -> filmDirectors.getOrDefault(film.getId(), Collections.emptySet()).contains(directorId))
                .collect(Collectors.toList());
    }

    @Override
    public int countLikes(int filmId) {
        log.debug("Подсчет лайков для фильма с id {} в памяти", filmId);
        return filmLikes.getOrDefault(filmId, Collections.emptySet()).size();
    }

    @Override
    public void deleteDirectorsFromFilm(int filmId) {
        log.debug("Удаление всех режиссеров для фильма с id {} в памяти", filmId);
        filmDirectors.remove(filmId);
        log.info("Режиссеры удалены для фильма {}", filmId);
    }

    @Override
    public void addDirectorToFilm(int filmId, int directorId) {
        log.debug("Добавление режиссера {} к фильму {} в памяти", directorId, filmId);
        filmDirectors.computeIfAbsent(filmId, k -> new HashSet<>()).add(directorId);
        log.info("Режиссер {} добавлен к фильму {}", directorId, filmId);
    }

    @Override
    public List<Film> searchFilms(String query, boolean searchByTitle, boolean searchByDirector) {
        log.debug("Поиск фильмов по запросу '{}' с параметрами by={} в памяти", query, List.of(searchByTitle ? "title" : "", searchByDirector ? "director" : "").stream().filter(s -> !s.isEmpty()).collect(Collectors.joining(",")));
        String lowerQuery = query.toLowerCase();

        List<Film> foundFilms = films.values().stream()
                .filter(film -> {
                    boolean matches = false;

                    if (searchByTitle && film.getName().toLowerCase().contains(lowerQuery)) {
                        matches = true;
                    }

                    if (searchByDirector && !matches) {
                        Set<Integer> filmDirectorIds = filmDirectors.getOrDefault(film.getId(), Collections.emptySet());
                        matches = filmDirectorIds.stream()
                                .map(directorStorage::findDirectorById)
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .anyMatch(director -> director.getName().toLowerCase().contains(lowerQuery));
                    }

                    return matches;
                })
                .sorted((f1, f2) -> {
                    int likes1 = filmLikes.getOrDefault(f1.getId(), Collections.emptySet()).size();
                    int likes2 = filmLikes.getOrDefault(f2.getId(), Collections.emptySet()).size();
                    return Integer.compare(likes2, likes1);
                })
                .collect(Collectors.toList());

        log.info("Найдено {} фильмов в памяти по запросу '{}'", foundFilms.size(), query);
        return foundFilms;
    }


    @Override
    public void deleteFilm(int filmId) {
        log.info("Удаление фильма с id: {} из памяти", filmId);
        if (!films.containsKey(filmId)) {
            throw new NotFoundException(String.format("Фильм с id = %d не найден", filmId));
        }

        filmLikes.remove(filmId);
        filmDirectors.remove(filmId);

        films.remove(filmId);
        log.debug("Фильм с id = {} удален из памяти", filmId);
    }


    @Override
    public Set<Integer> findFilmLikesByUserId(Integer userId) {
        log.debug("Поиск лайков для пользователя {} в памяти", userId);
        Set<Integer> likedFilmIds = new HashSet<>();
        for (Map.Entry<Integer, Set<Integer>> entry : filmLikes.entrySet()) {
            Integer filmId = entry.getKey();
            Set<Integer> userIdsWhoLiked = entry.getValue();
            if (userIdsWhoLiked != null && userIdsWhoLiked.contains(userId)) {
                likedFilmIds.add(filmId);
            }
        }
        log.debug("Найдено {} лайков для пользователя {} в памяти", likedFilmIds.size(), userId);
        return likedFilmIds;
    }

    @Override
    public Map<Integer, Set<Integer>> findAllUsersLikes() {
        log.debug("Поиск всех лайков всех пользователей в памяти");
        Map<Integer, Set<Integer>> usersLikes = new HashMap<>();
        for (Map.Entry<Integer, Set<Integer>> entry : filmLikes.entrySet()) {
            Integer filmId = entry.getKey();
            Set<Integer> userIds = entry.getValue();
            if (userIds != null) {
                for (Integer userId : userIds) {
                    usersLikes.computeIfAbsent(userId, k -> new HashSet<>()).add(filmId);
                }
            }
        }
        log.debug("Собрана карта лайков для {} пользователей в памяти", usersLikes.size());
        return usersLikes;
    }

    @Override
    public List<Film> findFilmsByIds(List<Integer> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) {
            log.debug("Список ID фильмов для поиска в памяти пуст");
            return Collections.emptyList();
        }
        log.debug("Поиск фильмов в памяти по списку ID: {}", filmIds);
        List<Film> foundFilms = films.values().stream()
                .filter(film -> film != null && film.getId() != null && filmIds.contains(film.getId()))
                .collect(Collectors.toList());
        log.debug("Найдено {} фильмов в памяти по списку ID", foundFilms.size());
        return foundFilms;
    }
}