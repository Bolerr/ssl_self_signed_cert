package org.example.ssl.web

import groovy.util.logging.Slf4j
import io.swagger.annotations.Api
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

import java.security.Principal

@RestController
@RequestMapping(value = '/api/v1.0/user', produces = MediaType.APPLICATION_JSON_VALUE)
@Api(value = 'Example/v1.0/user', description = 'User - returns information on authenticated user')
@Slf4j
class UserController {

    /**
     * Retrieves Principal of logged in user
     * @return {@link Principal}
     */
    @RequestMapping(value = ['/', '/login'], method = RequestMethod.GET)
    ResponseEntity<Principal> getUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication()
        Object loggedInUser = authentication.getPrincipal()

        return ResponseEntity.ok(loggedInUser)
    }
}
