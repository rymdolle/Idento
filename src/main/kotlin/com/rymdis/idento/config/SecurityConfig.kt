package com.rymdis.idento.config

import com.rymdis.idento.service.DatabaseUserDetailsService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.DelegatingPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {
    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        userAuthenticationManager: AuthenticationManager,
    ): SecurityFilterChain {

        return http
            .csrf { it.disable() }
            .securityMatcher("/api/${ApiVersion.V1}/**")
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authenticationManager(userAuthenticationManager)
            .authorizeHttpRequests {
                it.requestMatchers("/api/${ApiVersion.V1}/auth/key/**").permitAll()
                it.anyRequest().authenticated()
            }
            .httpBasic { }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { }
            }
            .build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        val encoderId = "pbkdf2"
        val encoders = mapOf<String, PasswordEncoder>(
            "bcrypt" to BCryptPasswordEncoder(),
            encoderId to Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8(),
        )
        return DelegatingPasswordEncoder(encoderId, encoders)
    }

    @Bean
    fun userAuthenticationManager(
        databaseUserDetailsService: DatabaseUserDetailsService,
        passwordEncoder: PasswordEncoder,
        jwtProvider: JwtAuthenticationProvider,
    ): AuthenticationManager {
        val userProvider = DaoAuthenticationProvider()
        userProvider.setUserDetailsService { username ->
            databaseUserDetailsService.loadUserByUsername(username)
        }
        userProvider.setPasswordEncoder(passwordEncoder)
        return ProviderManager(userProvider, jwtProvider)
    }
}
