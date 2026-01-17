package com.rymdis.idento.controller

import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.KeyType
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.proc.SecurityContext
import com.rymdis.idento.config.ApiVersion
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.User
import org.springframework.security.oauth2.jose.jws.JwsAlgorithms
import org.springframework.security.oauth2.jwt.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.time.temporal.ChronoUnit


private val log = KotlinLogging.logger {}

@RestController
class IdentoController(
    private val jwtEncoder: JwtEncoder,
    private val jwkSource: ImmutableJWKSet<SecurityContext>,
) {

    @PostMapping("/api/${ApiVersion.V1}/auth/login", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun login(
        @AuthenticationPrincipal user: User,
        @Value("\${app.security.token.ttl:1800}") ttl : Long,
        @Value("\${app.security.token.issuer:Idento}") issuer : String
    ): Map<String, Any> {
        val now = Instant.now()
        val claims = JwtClaimsSet.builder()
            .issuer(issuer)
            .issuedAt(now)
            .claim("authorities", user.authorities.map { it.authority })
            .subject(user.username)
            .expiresAt(now.plus(ttl, ChronoUnit.SECONDS))
            .build()
        val key = jwkSource.jwkSet.keys.find { it.keyType == KeyType.EC }!!
        val algorithm = when ((key as ECKey).curve) {
            Curve.P_256 -> JwsAlgorithms.ES256
            Curve.P_384 -> JwsAlgorithms.ES384
            Curve.P_521 -> JwsAlgorithms.ES512
            else -> throw IllegalArgumentException("Unsupported curve: ${key.curve}")
        }
        val headers = JwsHeader.with { algorithm }
            .keyId(key.keyID)
            .type("JWT")
            .build()
        val params = JwtEncoderParameters.from(headers, claims)
        val token = jwtEncoder.encode(params).tokenValue

        log.debug { "Generated token: $token" }
        return mapOf("token" to token)
    }

    @GetMapping("/api/${ApiVersion.V1}/auth/verify", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun verify(@AuthenticationPrincipal jwt: Jwt?): Map<String, Any> {
        jwt ?: throw BadCredentialsException("Invalid token")
        return mapOf(
            "claims" to jwt.claims,
            "headers" to jwt.headers,
            "token" to jwt.tokenValue,
        )
    }

    @GetMapping("/api/${ApiVersion.V1}/auth/key/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getKey(@PathVariable id: String): Map<String, Any> {
        val key = jwkSource.jwkSet.keys.find { it.keyID == id }
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        return key.toPublicJWK().toJSONObject()
    }

    @GetMapping("/.well-known/jwks.json", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun jwks(): Map<String, List<Map<String, Any>>> {
        val keys = jwkSource.jwkSet.keys.map {
            it.toPublicJWK().toJSONObject()
        }

        return mapOf("keys" to keys)
    }

}
