package com.rymdis.idento.exception

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

open class DomainException(
    reason: String,
    status: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
) : ResponseStatusException(status, reason)
