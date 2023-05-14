package ru.it.lab.dao;

import ru.it.lab.entities.AbstractEntity;

import java.util.List;

public interface AbstractDao<T extends AbstractEntity> {
    T getById(Long id);

    List<T> getAll();

    void deleteById(Long id);

    void update(T entity);

    T create(T entity);
}