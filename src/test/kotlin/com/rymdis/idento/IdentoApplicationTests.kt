package com.rymdis.idento

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IdentoApplicationTests {
    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Test
    fun contextLoads() {
    }

    @Test
    fun testAuth() {
        val headers = HttpHeaders()
        headers.setBasicAuth("admin", "nimda")
        val response = restTemplate.withBasicAuth("admin", "nimda")
            .getForEntity("/admin", String::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

}
