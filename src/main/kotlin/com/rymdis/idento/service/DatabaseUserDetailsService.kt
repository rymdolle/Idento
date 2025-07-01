package com.rymdis.idento.service

import com.rymdis.idento.repository.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.annotation.Primary
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger {}

@Service
@Primary
class DatabaseUserDetailsService(
    private val userRepository: UserRepository,
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        log.debug { "Loading user from database: $username" }

        val userEntity = userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("User not found: $username")

        // Create authorities from both roles and authorities
        val authorities = mutableListOf<SimpleGrantedAuthority>()

        // Add role-based authorities with ROLE_ prefix
        userEntity.roles.forEach { role ->
            authorities.add(SimpleGrantedAuthority("ROLE_$role"))
        }

        // Add direct authorities
        userEntity.authorities.forEach { authority ->
            authorities.add(SimpleGrantedAuthority(authority))
        }

        return User.builder()
            .username(userEntity.username)
            .password(userEntity.password)
            .authorities(authorities)
            .build()
    }
}
