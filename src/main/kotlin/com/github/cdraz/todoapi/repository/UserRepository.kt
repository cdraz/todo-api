package com.github.cdraz.todoapi.repository

import com.github.cdraz.todoapi.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<UserEntity, Long> {
    fun existsByEmail(email: String): Boolean
    fun findByEmail(email: String): UserEntity?
}
