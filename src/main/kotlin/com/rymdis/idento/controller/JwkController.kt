package com.rymdis.idento.controller

import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.proc.SecurityContext
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class JwkController(
    private val jwkSource: ImmutableJWKSet<SecurityContext>,
) {
    @GetMapping("/.well-known/jwks.json",
        produces = [MediaType.APPLICATION_JSON_VALUE])
    fun jwks(): Map<String, List<Map<String, Any>>> {
        val keys = jwkSource.jwkSet.keys.map {
            it.toPublicJWK().toJSONObject()
        }

        return mapOf("keys" to keys)
    }
}
