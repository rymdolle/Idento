package com.rymdis.idento.model

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.util.*

@Entity
@Table(name = "idento_users")
class User(
    @Id
    @Column(columnDefinition = "UUID")
    val id: UUID = UUID.randomUUID(),

    @Column(unique = true, nullable = false)
    val username: String,

    @Column(nullable = false)
    var password: String,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "idento_roles", joinColumns = [JoinColumn(name = "id")])
    @Column(name = "role")
    var roles: MutableSet<String> = mutableSetOf(),

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "idento_authorities", joinColumns = [JoinColumn(name = "id")])
    @Column(name = "authority")
    var authorities: MutableSet<String> = mutableSetOf(),
) {
    constructor() : this(
        id = UUID.randomUUID(),
        username = "",
        password = "",
        roles = mutableSetOf(),
        authorities = mutableSetOf(),
    )
}

@Configuration
@ConfigurationProperties(prefix = "app.security")
class UserProperties {
    var users: List<User> = emptyList()
}
