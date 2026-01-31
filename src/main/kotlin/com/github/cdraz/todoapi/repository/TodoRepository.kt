package com.github.cdraz.todoapi.repository

import com.github.cdraz.todoapi.entity.TodoEntity
import org.springframework.data.jpa.repository.JpaRepository

interface TodoRepository : JpaRepository<TodoEntity, Long> {
    // underscore to demonstrate relationship traversal; we want todo.user.id not todo.userId
    fun findByIdAndUser_Id(id: Long, userId: Long): TodoEntity?
}
