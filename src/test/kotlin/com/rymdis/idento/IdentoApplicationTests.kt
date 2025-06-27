package com.rymdis.idento

import com.rymdis.idento.config.JwtConfig
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

class IdentoApplicationTests {

    @Test
    @OptIn(kotlin.uuid.ExperimentalUuidApi::class)
    fun uuidHex() {
        println(Uuid.random().toHexString())
    }
    @Test
    @OptIn(kotlin.uuid.ExperimentalUuidApi::class)
    fun uuidString() {
        println(Uuid.random().toString())
    }
    @Test
    fun generateECKey() {
        val key = JwtConfig.generateECKey()
        println("kid: ${key.keyID}")
        println("kty: ${key.keyType}")
        println("crv: ${key.curve}")
        println("d: ${key.d}")
        println("x: ${key.x}")
        println("y: ${key.y}")
    }

}
