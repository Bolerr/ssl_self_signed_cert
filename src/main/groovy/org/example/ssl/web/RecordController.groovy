package org.example.ssl.web

import groovy.util.logging.Slf4j
import io.swagger.annotations.Api
import org.example.ssl.domain.Record
import org.example.ssl.service.RecordService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(value = '/api/v1.0/record', produces = MediaType.APPLICATION_JSON_VALUE)
@Api(value = 'Example/v1.0/record', description = 'Record resource')
@Slf4j
class RecordController {

    RecordService service

    RecordController(RecordService service) {
        this.service = service
    }

    /**
     * Retrieves a collect of Record objects
     * @return {@link Collection < Record >}
     */
    @RequestMapping(value = '/', method = RequestMethod.GET)
    ResponseEntity<Collection<Record>> list() {
        Collection<Record> results = service.list()
        return ResponseEntity.ok(results)
    }

    /**
     * Retrieves the requested Record
     * @param id {@link Long}
     * @return {@link Record}
     */
    @RequestMapping(value = '/{id}', method = RequestMethod.GET)
    ResponseEntity<Record> get(@PathVariable('id') Long id) {
        Record result = service.get(id)
        if (result) {
            return ResponseEntity.ok(result)
        } else {
            return ResponseEntity.notFound().build()
        }
    }

    /**
     * Updates the submitted Record
     * @param updateObject {@link Record}
     * @return {@link Record}
     */
    @RequestMapping(value = '/{id}', method = RequestMethod.PUT)
    ResponseEntity<Record> update(@RequestBody Record updateObject) {
        log.debug("update($updateObject) called")

        return saveOrUpdate(updateObject)
    }

    /**
     * Saves a new submitted Record
     * @param resource {@link Record}
     * @return {@link Record}
     */
    @RequestMapping(value = '/', method = RequestMethod.POST)
    ResponseEntity<Record> saveNew(@RequestBody Record resource) {
        log.debug("saveNew($resource) called")

        //For security safety, make sure id is not set
        resource.id = null

        return saveOrUpdate(resource)
    }

    /**
     * Deletes resource associated with id
     * @param id {@link Long}
     * @return {@link Map < String , Long >} id of deleted object
     */
    @RequestMapping(value = '/{id}', method = RequestMethod.DELETE)
    ResponseEntity<Map<String, Long>> delete(@PathVariable('id') Long id) {

        service.delete(id)
        return ResponseEntity.ok(['id': id])
    }

    protected ResponseEntity<Record> saveOrUpdate(Record object) {

        Record updated = service.update(object)

        return ResponseEntity.ok(updated)
    }
}
