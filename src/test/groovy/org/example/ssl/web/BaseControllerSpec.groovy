package org.example.ssl.web

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Ignore
import spock.lang.Specification

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup

@JsonTest
@Ignore
abstract class BaseControllerSpec extends Specification {

    MockMvc mockMvc

    @Autowired
    ObjectMapper objectMapper

    MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        return new MappingJackson2HttpMessageConverter(objectMapper: objectMapper)
    }

    void setup() {
        mockMvc = standaloneSetup(getController())
                .setMessageConverters(mappingJackson2HttpMessageConverter())
                .build()
    }

    abstract def getController()
}