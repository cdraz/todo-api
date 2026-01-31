package com.github.cdraz.todoapi.repository

import com.github.cdraz.todoapi.entity.TodoEntity
import org.springframework.data.jpa.repository.JpaRepository

interface TodoRepository : JpaRepository<TodoEntity, Long> {
    fun findByIdAndUserId(id: Long, userId: Long): TodoEntity?
}
