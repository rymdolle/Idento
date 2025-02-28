package com.rymdis.idento.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.NoOpPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver
import org.springframework.security.web.SecurityFilterChain
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger {}

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(SecurityProperties::class)
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
                    // Resolve bearer first, then check JWT cookie
                    val defaultBearer = DefaultBearerTokenResolver()
                    defaultBearer.resolve(request) ?: request.cookies?.find { it.name == "JWT" }?.value
                }
            }
            .build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return NoOpPasswordEncoder.getInstance()
    }

    @Bean
    fun userAuthenticationManager(
        userDetailsService: UserDetailsService,
        passwordEncoder: PasswordEncoder,
        jwtProvider: JwtAuthenticationProvider,
    ): AuthenticationManager {
        val userProvider = DaoAuthenticationProvider()
        userProvider.setUserDetailsService(userDetailsService)
        userProvider.setPasswordEncoder(passwordEncoder)
        return ProviderManager(userProvider, jwtProvider)
    }
}

@ConfigurationProperties(prefix = "app.security")
data class SecurityProperties(val users: List<UserProperties>)

data class UserProperties(
    val username: String,
    val password: String,
    val roles: List<String> = emptyList(),
    val authorities: List<String> = emptyList()
)

@Service
@Primary
class YamlUserDetailsManager(private val securityProperties: SecurityProperties) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        return securityProperties.users
            .find { it.username == username }
            ?.let { user ->
                User.withUsername(user.username)
                    .password(user.password)
                    .roles(*user.roles.toTypedArray())
                    .authorities(*user.authorities.toTypedArray())
                    .build()
            } ?: throw UsernameNotFoundException("User not found")
    }
}