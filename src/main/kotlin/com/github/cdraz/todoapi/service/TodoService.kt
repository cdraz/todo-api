package com.github.cdraz.todoapi.service

import com.github.cdraz.todoapi.dto.CreateTodoRequest
import com.github.cdraz.todoapi.dto.TodoResponse
import com.github.cdraz.todoapi.entity.TodoEntity
import com.github.cdraz.todoapi.exception.UserNotFoundException
import com.github.cdraz.todoapi.repository.TodoRepository
import com.github.cdraz.todoapi.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class TodoService(
    private val todoRepository: TodoRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun createTodo(userId: Long, request: CreateTodoRequest): TodoResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException(userId) }

        val todo = TodoEntity(
            title = request.title,
            user = user
        )

        val savedTodo = todoRepository.save(todo)

        return TodoResponse(
            id = savedTodo.id,
            title = savedTodo.title,
            completed = savedTodo.completed,
            createdAt = savedTodo.createdAt,
            userId = savedTodo.user.id
        )
    }
}
