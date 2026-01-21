package com.rymdis.idento.service

import com.rymdis.idento.exception.AlreadyExistsExceptions
import com.rymdis.idento.exception.BadArgumentException
import com.rymdis.idento.exception.NotFoundException
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
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) : UserDetailsService {
    @Transactional
    override fun loadUserByUsername(username: String): UserDetails {
        val user = findByUsername(username)
            ?: throw UsernameNotFoundException("User not found: $username")

        // Create authorities from both roles and authorities
        val authorities = user.authorities.map {
            SimpleGrantedAuthority(it)
        }.toMutableSet()

        // Add role-based authorities with ROLE_ prefix
        user.roles.forEach { role ->
            authorities.add(SimpleGrantedAuthority("ROLE_$role"))
        }

        return User.builder()
            .username(user.username)
            .password(user.password)
            .authorities(authorities)
            .build()
    }

    @Transactional
    fun createUser(username: String, password: String): ApplicationUser {
        if (userRepository.existsByUsername(username)) {
            throw AlreadyExistsExceptions("User")
        }
        val encodedPassword = passwordEncoder.encode(password)
        return userRepository.save(ApplicationUser(
            username = username,
            password = encodedPassword,
        ))
    }

    @Transactional
    fun deleteUser(id: UUID) {
        if (!userRepository.existsById(id)) {
            throw NotFoundException("User")
        }
        userRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    fun findByUsername(username: String): ApplicationUser? {
        return userRepository.findByUsername(username)
    }

    @Transactional(readOnly = true)
    fun getById(id: UUID): ApplicationUser {
        return userRepository.findById(id).orElseThrow {
            throw NotFoundException("User")
        }
    }

    @Transactional(readOnly = true)
    fun existsByUsername(username: String): Boolean {
        return userRepository.existsByUsername(username)
    }

    @Transactional(readOnly = true)
    fun existsById(id: UUID): Boolean {
        return userRepository.existsById(id)
    }

    @Transactional
    fun addRole(id: UUID, role: String) {
        if (role.startsWith("ROLE_")) {
            throw BadArgumentException("Role cannot start with ROLE_")
        }
        val user = getById(id)
        user.roles.add(role)
        userRepository.save(user)
    }

    @Transactional
    fun removeRole(id: UUID, role: String) {
        val user = getById(id)
        user.roles.remove(role)
        userRepository.save(user)
    }

    @Transactional
    fun addAuthority(id: UUID, authority: String) {
        if (authority.startsWith("ROLE_")) {
            throw BadArgumentException("Authority cannot start with ROLE_")
        }
        val user = getById(id)
        user.authorities.add(authority)
        userRepository.save(user)
    }

    @Transactional
    fun removeAuthority(id: UUID, authority: String) {
        val user = getById(id)
        user.authorities.remove(authority)
        userRepository.save(user)
    }

}
