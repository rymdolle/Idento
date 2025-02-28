package com.rymdis.idento.config

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.proc.JWSVerificationKeySelector
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor
import com.nimbusds.jwt.proc.DefaultJWTProcessor
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import java.security.KeyPairGenerator
import java.security.interfaces.ECPublicKey
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.*

private val log = KotlinLogging.logger {}

@Configuration
class JwtConfig() {

    @Bean
    fun jwkSource(): ImmutableJWKSet<SecurityContext> {
        val keys = listOf(
            generateECKey(Curve.P_521),
        )
        val jwkSet = JWKSet(keys)
        return ImmutableJWKSet<SecurityContext>(jwkSet)
    }

    @Bean
    fun jwtEncoder(jwkSource: ImmutableJWKSet<SecurityContext>): JwtEncoder {
        return NimbusJwtEncoder(jwkSource)
    }

    @Bean
    fun jwtDecoder(jwkSource: ImmutableJWKSet<SecurityContext>): JwtDecoder {
        val processor: ConfigurableJWTProcessor<SecurityContext> = DefaultJWTProcessor()
        val algorithms = setOf(
            JWSAlgorithm.RS256,
            JWSAlgorithm.RS384,
            JWSAlgorithm.RS512,
            JWSAlgorithm.ES256,
            JWSAlgorithm.ES384,
            JWSAlgorithm.ES512,
        )
        val selector = JWSVerificationKeySelector(algorithms, jwkSource)
        processor.jwsKeySelector = selector
        processor.setJWTClaimsSetVerifier { claims, context -> }
        return NimbusJwtDecoder(processor)
    }

    private fun generateRSAKey(keySize: Int = 2048, alg: JWSAlgorithm = JWSAlgorithm.RS256): RSAKey {
        val kpg = KeyPairGenerator.getInstance("RSA")
        kpg.initialize(keySize)
        log.info { "Generating RSA key $keySize" }
        val key = kpg.generateKeyPair()
        return RSAKey.Builder(key.public as RSAPublicKey)
            .privateKey(key.private as RSAPrivateKey)
            .keyID(UUID.randomUUID().toString())
            .keyUse(KeyUse.SIGNATURE)
            .algorithm(alg)
            .build()
    }

    private fun generateECKey(curve: Curve = Curve.P_256): ECKey {
        if (curve !in listOf(Curve.P_256, Curve.P_384, Curve.P_521)) {
            throw IllegalArgumentException("Invalid curve")
        }
        val keySize = curve.toECParameterSpec().order.bitLength()
        val kpg = KeyPairGenerator.getInstance("EC")
        kpg.initialize(keySize)
        log.info { "Generating EC key $curve" }
        val key = kpg.generateKeyPair()
        return ECKey.Builder(curve, key.public as ECPublicKey)
            .privateKey(key.private)
            .keyID(UUID.randomUUID().toString())
            .keyUse(KeyUse.SIGNATURE)
            .build()
    }
}
