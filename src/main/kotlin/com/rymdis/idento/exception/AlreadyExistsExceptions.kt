package com.rymdis.idento.exception

import org.springframework.http.HttpStatus

class AlreadyExistsExceptions(
    entity: String,
) : DomainException("$entity already exists", HttpStatus.CONFLICT)
