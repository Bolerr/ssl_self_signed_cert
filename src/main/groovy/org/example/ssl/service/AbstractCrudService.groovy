package org.example.ssl.service

import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Service

@Service
abstract class AbstractCrudService<T, ID, R extends PagingAndSortingRepository> {

    R repository

    AbstractCrudService(R repository) {
        this.repository = repository
    }

    Collection<T> list() {
        return repository.findAll()
    }

    T get(ID id) {
        return repository.findOne(id)
    }

    T save(T entity) {
        return repository.save(entity)
    }

    Collection<T> save(Collection<T> entities) {
        return repository.save(entities)
    }

    T update(T entity) {
        return save(entity)
    }

    void delete(ID id) {
        repository.delete(id)
    }
}
