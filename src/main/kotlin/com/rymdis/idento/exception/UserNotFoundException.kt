package com.rymdis.idento.exception

import org.springframework.http.HttpStatus

class UserNotFoundException
    : DomainException("User not found", HttpStatus.NOT_FOUND)
