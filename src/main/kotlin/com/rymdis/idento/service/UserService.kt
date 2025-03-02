package com.rymdis.idento.service

import com.rymdis.idento.model.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    @Transactional
    fun createUser(user: UserEntity): UserEntity {
        user.password = passwordEncoder.encode(user.password)
        return userRepository.save(user)
    }

    @Transactional(readOnly = true)
    fun findByUsername(username: String): UserEntity? {
        return userRepository.findByUsername(username)
    }
}

@Repository
interface UserRepository : JpaRepository<UserEntity, UUID> {
    fun findByUsername(username: String): UserEntity?
}
