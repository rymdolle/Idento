package com.rymdis.idento.config

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.proc.JWSVerificationKeySelector
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jose.util.Base64URL
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor
import com.nimbusds.jwt.proc.DefaultJWTProcessor
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import java.security.KeyPairGenerator
import java.security.interfaces.ECPublicKey
import java.util.*

private val log = KotlinLogging.logger {}

@Configuration
@EnableConfigurationProperties(JwkProperties::class)
class JwtConfig(private val jwkProperties: JwkProperties) {

    @Bean
    fun jwkSource(): ImmutableJWKSet<SecurityContext> {
        val keys = jwkProperties.keys.map { key ->
            if (!key.file.isNullOrBlank())
                key.fromFileToJWK()
            else
                key.toJWK()
        }
        val jwkSet = JWKSet(keys.ifEmpty { listOf(
            generateECKey(),
        ) })
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
            JWSAlgorithm.ES256,
            JWSAlgorithm.ES384,
            JWSAlgorithm.ES512,
        )
        val selector = JWSVerificationKeySelector(algorithms, jwkSource)
        processor.jwsKeySelector = selector
        processor.setJWTClaimsSetVerifier { claims, context -> }
        return NimbusJwtDecoder(processor)
    }

    companion object {
        fun generateECKey(curve: Curve = Curve.P_256): ECKey {
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

}

@Configuration
@ConfigurationProperties(prefix = "app.security.jwks")
class JwkProperties {
    var keys: List<JwkKey> = emptyList()

    class JwkKey {
        lateinit var kid: String
        lateinit var kty: String
        var use: String = KeyUse.SIGNATURE.value
        var crv: String? = null
        var d: String? = null
        var x: String? = null
        var y: String? = null
        var file: String? = null

        fun toJWK(): JWK {
            kty.equals("EC", ignoreCase = true) || throw IllegalArgumentException("Key type must be EC")
            return ECKey.Builder(Curve.parse(crv), Base64URL(x), Base64URL(y))
                    .d(Base64URL(d))
                    .keyID(kid)
                    .keyUse(KeyUse.parse(use))
                    .build()
        }

        fun fromFileToJWK(): JWK {
            val pem = file?.let { ClassPathResource(file!!).inputStream.bufferedReader().readText() }
                ?: throw IllegalArgumentException("Missing file")
            val jwk = JWK.parseFromPEMEncodedObjects(pem) as ECKey
            return ECKey.Builder(jwk)
                .keyID(kid)
                .keyUse(KeyUse.parse(use))
                .build()
        }
    }
}