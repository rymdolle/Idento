package com.rymdis.idento

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

}
