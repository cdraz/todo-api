package com.github.cdraz.todoapi.repository

import com.github.cdraz.todoapi.entity.TodoEntity
import com.github.cdraz.todoapi.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface TodoRepository : JpaRepository<TodoEntity, Long> {
    fun findAllByUser(user: UserEntity): List<TodoEntity>
    fun existsByIdAndUser(id: Long, user: UserEntity): Boolean
    fun findAllByUserId(userId: Long): List<TodoEntity>
}
