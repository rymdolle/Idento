package com.rymdis.idento.dto

data class UserDto(
    val username : String,
    val password : String,
    val roles : List<String>,
    val authorities : List<String>,
)
