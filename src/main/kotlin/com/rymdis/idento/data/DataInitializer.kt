package com.rymdis.idento.data

import com.rymdis.idento.model.ApplicationUser
import com.rymdis.idento.service.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.context.properties.ConfigurationProperties
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
                if (!userService.existsByUsername(user.username)) {
                    userService.createUser(user)
                } else {
                    log.warn { "User ${user.username} already exists. Skipping." }
                }
            }
        }
    }
}

@Configuration
@ConfigurationProperties(prefix = "app.security")
class UserProperties {
    var users: List<ApplicationUser> = emptyList()
}
