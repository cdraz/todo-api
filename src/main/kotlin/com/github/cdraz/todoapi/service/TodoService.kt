package com.github.cdraz.todoapi.service

import com.github.cdraz.todoapi.dto.CreateTodoRequest
import com.github.cdraz.todoapi.dto.TodoResponse
import com.github.cdraz.todoapi.dto.UpdateTodoCompletionRequest
import com.github.cdraz.todoapi.dto.UpdateTodoTitleRequest
import com.github.cdraz.todoapi.entity.TodoEntity
import com.github.cdraz.todoapi.exception.TodoNotFoundForUserException
import com.github.cdraz.todoapi.exception.UserNotFoundException
import com.github.cdraz.todoapi.repository.TodoRepository
import com.github.cdraz.todoapi.repository.TodoView
import com.github.cdraz.todoapi.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class TodoService(
    private val todoRepository: TodoRepository,
    private val userRepository: UserRepository
) {

    fun getTodo(userId: Long, todoId: Long): TodoResponse {
        val todoView = todoRepository.findViewByIdAndUserId(todoId, userId)
        if (todoView != null) return todoView.toResponse()
        // if nothing found, check if user exists
        if (!userRepository.existsById(userId)) {
            throw UserNotFoundException(userId)
        }
        throw TodoNotFoundForUserException(todoId, userId)
    }

    fun getTodosForUser(userId: Long): List<TodoResponse> {
        if (!userRepository.existsById(userId)) {
            throw UserNotFoundException(userId)
        }
        return todoRepository.findAllProjectedByUser_IdOrderByCreatedAtDesc(userId)
            .map { it.toResponse() }
    }

    @Transactional
    fun createTodo(userId: Long, request: CreateTodoRequest): TodoResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException(userId) }

        val todo = TodoEntity(
            title = request.title,
            user = user
        )

        val savedTodo = todoRepository.save(todo)

        return savedTodo.toResponse()
    }

    @Transactional
    fun updateTodoTitle(userId: Long, todoId: Long, request: UpdateTodoTitleRequest): TodoResponse {
        val todo = todoRepository.findByIdAndUser_Id(todoId, userId)
            ?: throw TodoNotFoundForUserException(todoId, userId)

        if (todo.title != request.title) {
            todo.title = request.title
        }

        return todo.toResponse()
    }


    @Transactional
    fun updateTodoCompletion(userId: Long, todoId: Long, request: UpdateTodoCompletionRequest): TodoResponse {
        val todo = todoRepository.findByIdAndUser_Id(todoId, userId)
            ?: throw TodoNotFoundForUserException(todoId, userId)

        if (todo.completed != request.completed) {
            todo.completed = request.completed
        }

        return todo.toResponse()
    }

    private fun TodoEntity.toResponse(): TodoResponse =
        TodoResponse(
            id = this.id,
            title = this.title,
            completed = this.completed,
            createdAt = this.createdAt,
            userId = this.user.id
        )

    private fun TodoView.toResponse(): TodoResponse =
        TodoResponse(
            id = id,
            title = title,
            completed = completed,
            createdAt = createdAt,
            userId = userId
        )

}
