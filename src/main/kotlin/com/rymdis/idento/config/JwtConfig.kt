package com.rymdis.idento.config

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.proc.SecurityContext
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.*

private val log = KotlinLogging.logger {}

@Configuration
class JwtConfig() {

    @Bean
    fun jwkSource(@Value("\${app.security.jwk.key-size:2048}") keySize: Int): ImmutableJWKSet<SecurityContext> {
        log.info { "Start generating RSA key pair of $keySize bits" }
        val kpg = KeyPairGenerator.getInstance("RSA")
        kpg.initialize(keySize)
        val key = kpg.generateKeyPair()
        log.info { "RSA key pair done." }
        val keys = RSAKey.Builder(key.public as RSAPublicKey)
            .privateKey(key.private as RSAPrivateKey)
            .keyID(UUID.randomUUID().toString())
            .keyUse(KeyUse.SIGNATURE)
        val jwkSet = JWKSet(keys.build())
        return ImmutableJWKSet<SecurityContext>(jwkSet)
    }

    @Bean
    fun jwtEncoder(jwkSource: ImmutableJWKSet<SecurityContext>): JwtEncoder {
        return NimbusJwtEncoder(jwkSource)
    }

    @Bean
    fun jwtDecoder(jwkSource: ImmutableJWKSet<SecurityContext>): JwtDecoder {
        val key = jwkSource.jwkSet.keys.first()
        val rsa = key.toRSAKey().toRSAPublicKey()
        return NimbusJwtDecoder.withPublicKey(rsa).build()
    }
}
