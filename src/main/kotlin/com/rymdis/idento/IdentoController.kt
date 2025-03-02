package com.rymdis.idento

import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.KeyType
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.proc.SecurityContext
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.User
import org.springframework.security.oauth2.jose.jws.JwsAlgorithms
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.time.temporal.ChronoUnit


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
            .claim("scp", user.authorities.map { it.authority })
            .subject(user.username)
            .expiresAt(now.plus(30, ChronoUnit.MINUTES))
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

        log.info { "Generated token: $token" }
        return ResponseEntity.ok().body(token)
    }

    @GetMapping("/api/dev/auth/verify", produces = ["application/json"])
    fun verify(@AuthenticationPrincipal jwt: Jwt?): Map<String, Any> {
        jwt ?: throw BadCredentialsException("Invalid token")
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
