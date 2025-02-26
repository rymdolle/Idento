package com.rymdis.idento.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
class JwtAuthenticationProvider(private val jwtDecoder: JwtDecoder) : AuthenticationProvider {
    override fun authenticate(authentication: Authentication): Authentication? {
        val bearer = authentication as BearerTokenAuthenticationToken
        try {
            val jwt = jwtDecoder.decode(bearer.token)
            return JwtAuthenticationToken(jwt, null)
        } catch (e: JwtException) {
            throw BadCredentialsException("Authentication token invalid", e)
        }
    }

    override fun supports(authentication: Class<*>): Boolean {
        return BearerTokenAuthenticationToken::class.java.isAssignableFrom(authentication)
    }
}