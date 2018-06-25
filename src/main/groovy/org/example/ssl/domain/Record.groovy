package org.example.ssl.domain

import groovy.transform.Canonical

import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@Table(name = 'record')
@Canonical
class Record {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = 'id', nullable = false, insertable = false, updatable = false)
    Long id

    @Column(name = 'record_number', nullable = false)
    @NotNull
    Integer recordNumber

    @Column(name = 'content', length = 255)
    String content
}
