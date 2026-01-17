package com.rymdis.idento.controller

import jakarta.servlet.RequestDispatcher
import jakarta.servlet.http.HttpServletRequest
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/error")
class IdentoErrorController : ErrorController {
    @RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun handleError(request: HttpServletRequest): ResponseEntity<Map<String, Any?>> {
        val status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE) as? Int
            ?: HttpStatus.INTERNAL_SERVER_ERROR.value()
        val body: Map<String, Any?> = mapOf(
            "timestamp" to Instant.now(),
            "status" to status,
            "error" to HttpStatus.resolve(status)?.reasonPhrase,
            "message" to request.getAttribute(RequestDispatcher.ERROR_MESSAGE),
            "path" to request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI),
        )
        return ResponseEntity.status(status).body(body)
    }
}
