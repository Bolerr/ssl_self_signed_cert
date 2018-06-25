package org.example.ssl.service

import org.example.ssl.domain.Record
import org.example.ssl.domain.RecordRepository
import org.springframework.stereotype.Service

@Service
class RecordService extends AbstractCrudService<Record, Long, RecordRepository> {

    RecordService(RecordRepository repository) {
        super(repository)
    }
}
