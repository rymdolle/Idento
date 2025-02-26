package com.rymdis.idento

import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.proc.SecurityContext
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.User
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import kotlin.time.Duration.Companion.hours


private val log = KotlinLogging.logger {}

@RestController
class IdentoController(
    private val jwtEncoder: JwtEncoder,
    private val jwkSource: ImmutableJWKSet<SecurityContext>,
) {
    @PostMapping("/api/dev/auth/login", produces = ["text/plain"])
    fun login(@AuthenticationPrincipal user: User): ResponseEntity<String> {
        val now = Instant.now()
        val claims = JwtClaimsSet.builder()
            .issuer("Idento")
            .issuedAt(now)
            .subject(user.username)
            .expiresAt(now.plusSeconds(2.hours.inWholeSeconds))
            .build()
        val headers = JwsHeader.with {"RS256"}
            .keyId(jwkSource.jwkSet.keys.first().keyID)
            .type("JWT")
            .build()
        val params = JwtEncoderParameters.from(headers, claims)
        val token = jwtEncoder.encode(params).tokenValue

        log.info { "Generated token: $token" }
        return ResponseEntity.ok().body(token)
    }

    @GetMapping("/api/dev/auth/verify", produces = ["application/json"])
    fun verify(@AuthenticationPrincipal jwt: Jwt): Map<String, Any> {
        return mapOf(
            "claims" to jwt.claims,
            "headers" to jwt.headers,
            "token" to jwt.tokenValue,
        )
    }

    @GetMapping("/.well-known/jwks.json", produces = ["application/json"])
    fun jwks(): Map<String, List<Map<String, Any>>> {
        val keys = jwkSource.jwkSet.keys.map {
            it.toPublicJWK().toJSONObject()
        }

        return mapOf("keys" to keys)
    }

}
