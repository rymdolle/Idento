package com.rymdis.idento.exception

import org.springframework.http.HttpStatus

class NotFoundException(
    entity: String,
) : DomainException("$entity not found", HttpStatus.NOT_FOUND)
