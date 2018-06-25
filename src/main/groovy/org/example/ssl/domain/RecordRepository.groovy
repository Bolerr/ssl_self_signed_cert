package org.example.ssl.domain

import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface RecordRepository extends PagingAndSortingRepository<Record, Long> {

}