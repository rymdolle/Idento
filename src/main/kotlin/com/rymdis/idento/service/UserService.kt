package com.rymdis.idento.service

import com.rymdis.idento.model.ApplicationUser
import com.rymdis.idento.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("User not found: $username")

        // Create authorities from both roles and authorities
        val authorities = mutableListOf<SimpleGrantedAuthority>()

        // Add role-based authorities with ROLE_ prefix
        user.roles.forEach { role ->
            authorities.add(SimpleGrantedAuthority("ROLE_$role"))
        }

        // Add direct authorities
        user.authorities.forEach { authority ->
            authorities.add(SimpleGrantedAuthority(authority))
        }

        return User.builder()
            .username(user.username)
            .password(user.password)
            .authorities(authorities)
            .build()
    }

    @Transactional
    fun createUser(user: ApplicationUser): ApplicationUser {
        user.password = passwordEncoder.encode(user.password)
        return userRepository.save(user)
    }

    @Transactional(readOnly = true)
    fun findByUsername(username: String): ApplicationUser? {
        return userRepository.findByUsername(username)
    }

    @Transactional(readOnly = true)
    fun existsByUsername(username: String): Boolean {
        return userRepository.existsByUsername(username)
    }
}
