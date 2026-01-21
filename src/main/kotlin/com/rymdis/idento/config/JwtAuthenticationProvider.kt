package com.rymdis.idento.config

import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component

@Component
class JwtAuthenticationProvider(private val jwtDecoder: JwtDecoder) : AuthenticationProvider {
    override fun authenticate(authentication: Authentication): Authentication? {
        val bearer = authentication as BearerTokenAuthenticationToken
        try {
            val jwt = jwtDecoder.decode(bearer.token)
            val scp = jwt.claims["scp"] as? List<*>
            val authorities = scp?.map {
                SimpleGrantedAuthority(it as String)
            }
            return JwtAuthenticationToken(jwt, authorities)
        } catch (e: JwtException) {
            throw BadCredentialsException("Authentication token invalid", e)
        }
    }

    override fun supports(authentication: Class<*>): Boolean {
        return BearerTokenAuthenticationToken::class.java.isAssignableFrom(authentication)
    }
}
