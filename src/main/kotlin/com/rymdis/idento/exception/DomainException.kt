package com.rymdis.idento.exception

import org.springframework.http.HttpStatus

open class DomainException(
    val reason: String,
    val status: HttpStatus,
) : Exception()
