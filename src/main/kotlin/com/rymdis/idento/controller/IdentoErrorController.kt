package com.rymdis.idento.controller

import com.rymdis.idento.exception.DomainException
import jakarta.servlet.RequestDispatcher
import jakarta.servlet.http.HttpServletRequest
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant

@RestControllerAdvice
@RestController
@RequestMapping("/error")
class IdentoErrorController : ErrorController {
    @RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun handleError(request: HttpServletRequest): ResponseEntity<Map<String, Any?>> {
        val status = (request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE) as? Int)?.let {
            HttpStatus.resolve(it)
        } ?: HttpStatus.INTERNAL_SERVER_ERROR
        return errorResponse(status, request)
    }

    @ExceptionHandler(DomainException::class)
    fun handleDomainException(ex: DomainException, request: HttpServletRequest): ResponseEntity<Map<String, Any?>> {
        return errorResponse(ex.status, request)
    }

    fun errorResponse(status: HttpStatus, request: HttpServletRequest) : ResponseEntity<Map<String, Any?>> {
        val body = mapOf(
            "timestamp" to Instant.now(),
            "status" to status.value(),
            "error" to status.reasonPhrase,
            "message" to request.getAttribute(RequestDispatcher.ERROR_MESSAGE),
            "path" to request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI),
        )
        return ResponseEntity.status(status).body(body)
    }
}
