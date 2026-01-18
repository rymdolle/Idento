package com.rymdis.idento.controller

import com.rymdis.idento.config.ApiVersion
import com.rymdis.idento.exception.MissingArgumentException
import com.rymdis.idento.model.ApplicationUser
import com.rymdis.idento.service.UserService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder
import java.util.*

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/${ApiVersion.V1}/user")
class UserController(
    private val userService: UserService,
) {
    @PostMapping(
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE])
    fun createUser(
        @RequestBody user : ApplicationUser,
        request: HttpServletRequest,
    ): ResponseEntity<Map<String, Any?>> {
        val createdUser = userService.createUser(user)
        val location = UriComponentsBuilder
            .fromPath(request.requestURI)
            .path("/{id}")
            .buildAndExpand(createdUser.id)
            .toUri()
        val body = mapOf(
            "id" to createdUser.id,
        )
        return ResponseEntity.created(location).body(body)
    }

    @GetMapping("/{id}",
        produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getUser(@PathVariable id: UUID) : Map<String, Any> {
        val user = userService.getById(id)
        return mapOf(
            "id" to user.id,
            "username" to user.username,
            "roles" to user.roles,
            "authorities" to user.authorities,
        )
    }

    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: UUID) {
        userService.deleteUser(id)
    }

    @PostMapping("/{id}/role/add",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE])
    fun addUserToRole(@PathVariable id: UUID, @RequestBody body: Map<String, String>) {
        val role = body["role"] ?: throw MissingArgumentException("role")
        userService.addRole(id, role)
    }

    @PostMapping("/{id}/authority/add",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE])
    fun addUserToAuthority(@PathVariable id: UUID, @RequestBody body: Map<String, String>) {
        val authority = body["authority"] ?: throw MissingArgumentException("authority")
        userService.addAuthority(id, authority)
    }
}
