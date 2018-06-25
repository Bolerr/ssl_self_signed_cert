package org.example.ssl.web

import com.blogspot.toomuchcoding.spock.subjcollabs.Collaborator
import com.blogspot.toomuchcoding.spock.subjcollabs.Subject
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.example.ssl.domain.Record
import org.example.ssl.service.RecordService
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import spock.lang.Shared

import static groovy.json.JsonOutput.toJson
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*

@Slf4j
class RecordControllerSpec extends BaseControllerSpec {

    @Collaborator
    RecordService service = Mock()

    @Subject
    RecordController controller

    @Shared
    String apiUrl = '/api/v1.0/record/'

    void "list returns a list of Record objects"() {
        given:
        Collection<Record> results = [
                new Record(id: 1, recordNumber: 1, content: 'record 1'),
                new Record(id: 2, recordNumber: 2, content: 'record 2')
        ]

        String expectedJson = '''
        [
            {
                "id": 1,
                "recordNumber": 1,
                "content": "record 1"
            },
            {
                "id": 2,
                "recordNumber": 2,
                "content": "record 2"
            }
        ]
        '''

        when:
        def response = mockMvc.perform(get(apiUrl).accept(APPLICATION_JSON))
                .andReturn().response

        then:
        1 * service.list() >> {
            return results
        }
        response.status == OK.value()
        JSONAssert.assertEquals(expectedJson, response.contentAsString, JSONCompareMode.NON_EXTENSIBLE)
    }

    void "get"() {
        given:
        Long requestedId = 1
        Record object = new Record(id: requestedId, recordNumber: 1, content: 'record 1')
        String expectedJson = '''
        {
            "id": 1,
            "recordNumber": 1,
            "content": "record 1"
        }
        '''

        when:
        def response = mockMvc.perform(get("$apiUrl/$requestedId").accept(APPLICATION_JSON))
                .andReturn().response
        log.info("get response: ${response.contentAsString}")

        then:
        1 * service.get(requestedId) >> {
            return object
        }
        response.status == OK.value()
        JSONAssert.assertEquals(expectedJson, response.contentAsString, JSONCompareMode.NON_EXTENSIBLE)
    }

    void "update"() {
        given:
        Record object = new Record(id: 1, recordNumber: 1, content: 'record 1')

        when:
        def response = mockMvc.perform(put("$apiUrl/123")
                .contentType(APPLICATION_JSON)
                .content(toJson(object)))
                .andReturn().response

        then:
        1 * service.update(object) >> {
            return object
        }
        response.status == OK.value()
    }

    void "saveNew sets id to null and calls saveOrUpdate"() {
        when:
        def response = mockMvc.perform(post(apiUrl)
                .contentType(APPLICATION_JSON)
                .content(toJson(new Record(id: 1, recordNumber: 1, content: 'record 1'))))
                .andReturn().response
        def content = new JsonSlurper().parseText(response.contentAsString)

        then: 'it calls save with null id'
        1 * service.update(_) >> new Record(id: 321)

        response.status == OK.value()

        content.id == 321
    }

    void "delete"() {
        given:
        Long requestedId = 123

        when:
        def response = mockMvc.perform(delete("$apiUrl/$requestedId")
                .contentType(APPLICATION_JSON))
                .andReturn().response
        log.info("delete response: ${response.contentAsString}")
        def content = new JsonSlurper().parseText(response.contentAsString)

        then:
        1 * service.delete(requestedId) >> {}

        response.status == OK.value()
        response.contentType == MediaType.APPLICATION_JSON_UTF8.toString()

        content.id == requestedId
    }

    void "saveOrUpdate"() {
        given:
        Record object = new Record(id: 1, recordNumber: 1, content: 'record 1')

        when:
        ResponseEntity<Record> response = controller.saveOrUpdate(object)

        then:
        1 * service.update(object) >> {
            return object
        }
        response.statusCode == OK
        response.body == object
    }
}
