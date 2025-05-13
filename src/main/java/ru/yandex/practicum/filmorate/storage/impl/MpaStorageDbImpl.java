package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.BaseStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
public class MpaStorageDbImpl extends BaseStorage<Mpa> implements MpaStorage {
    private static final String FIND_BY_ID_QUERY = " SELECT * FROM _mpa WHERE id = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM _mpa";
    private static final String COUNT_QUERY = "SELECT COUNT(*) FROM _mpa";

    public MpaStorageDbImpl(JdbcTemplate jdbc, RowMapper<Mpa> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Optional<Mpa> findMpaById(int mpaId) {
        return findOne(FIND_BY_ID_QUERY, mpaId);
    }

    @Override
    public List<Mpa> findAllMpa() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public boolean checkMpaCount(Integer needCount) {
        return (Objects.equals(count(COUNT_QUERY), needCount));
    }
}












