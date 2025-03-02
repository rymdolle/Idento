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
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver
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
            .securityMatcher("/api/**")
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authenticationManager(userAuthenticationManager)
            .authorizeHttpRequests {
                it.anyRequest().authenticated()
            }
            .httpBasic { basic ->
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwkSetUri("https://idento.rymdis.com/.well-known/jwks.json")
                }
                oauth2.bearerTokenResolver { request ->
                    val defaultBearer = DefaultBearerTokenResolver()
                    defaultBearer.resolve(request)
                }
            }
            .build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        val encoderId = "bcrypt"
        val encoders = mutableMapOf<String, PasswordEncoder>()
        encoders["bcrypt"] = BCryptPasswordEncoder()
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
