package com.rymdis.idento.repository

import com.rymdis.idento.model.ApplicationUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<ApplicationUser, UUID> {
    fun findByUsername(username: String): ApplicationUser?
    fun existsByUsername(username: String): Boolean
}
