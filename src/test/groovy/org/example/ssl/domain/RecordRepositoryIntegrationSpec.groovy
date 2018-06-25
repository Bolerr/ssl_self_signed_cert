package org.example.ssl.domain

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

@DataJpaTest
class RecordRepositoryIntegrationSpec extends Specification {

    @Autowired
    RecordRepository repository

    void cleanup() {
        repository.deleteAll()
    }

    void "basic CRUD operations should work"() {
        given:
        Record newObject = new Record(
                recordNumber: 1,
                content: 'content'
        )

        when: 'object is created and read back from DB'
        Record result = repository.save(newObject)
        Record read = repository.findOne(result.id)
        Long savedId = read.id

        then: 'read object is correct'
        read.id
        read.content == 'content'

        when: 'item is updated'
        read.content = 'test'
        Record updated = repository.save(read)
        Record updateResult = repository.findOne(updated.id)

        then: 'item was updated correctly'
        updateResult.id == savedId
        updateResult.content == 'test'

        when: 'deleted'
        repository.delete(updateResult.id)
        Record deleteResult = repository.findOne(updated.id)

        then: 'object is deleted and cannot be found'
        !deleteResult
    }
}
