package com.rymdis.idento.exception

import org.springframework.http.HttpStatus

class BadArgumentException(message: String)
    : DomainException(message, HttpStatus.BAD_REQUEST)
