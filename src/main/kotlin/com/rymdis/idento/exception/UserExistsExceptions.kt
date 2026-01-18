package com.rymdis.idento.exception

import org.springframework.http.HttpStatus

class UserExistsExceptions
    : DomainException("User already exists", HttpStatus.CONFLICT)