package com.rymdis.idento.data

import com.rymdis.idento.model.UserProperties
import com.rymdis.idento.service.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

private val log = KotlinLogging.logger {}

@Configuration
@EnableConfigurationProperties(UserProperties::class)
class DataInitializer(private val userProperties: UserProperties) {

    @Bean
    fun initializer(userService: UserService): CommandLineRunner {
        return CommandLineRunner {
            userProperties.users.forEach { user ->
                if (userService.findByUsername(user.username) == null) {
                    userService.createUser(user)
                } else {
                    log.warn { "User ${user.username} already exists. Skipping." }
                }
            }
        }
    }
}
