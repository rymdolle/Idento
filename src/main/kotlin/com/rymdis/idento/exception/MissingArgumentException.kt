package com.rymdis.idento.exception

import org.springframework.http.HttpStatus

class MissingArgumentException(name: String)
    : DomainException("Missing argument $name", HttpStatus.BAD_REQUEST)
