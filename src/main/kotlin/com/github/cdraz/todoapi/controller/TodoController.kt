package com.github.cdraz.todoapi.controller

import com.github.cdraz.todoapi.dto.CreateTodoRequest
import com.github.cdraz.todoapi.dto.TodoResponse
import com.github.cdraz.todoapi.dto.UpdateTodoCompletionRequest
import com.github.cdraz.todoapi.dto.UpdateTodoTitleRequest
import com.github.cdraz.todoapi.service.TodoService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.OK
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users/{userId}/todos")
class TodoController(
    private val todoService: TodoService
) {
    @PostMapping
    @ResponseStatus(CREATED)
    fun createTodo(
        @PathVariable userId: Long,
        @Valid @RequestBody request: CreateTodoRequest
    ): TodoResponse =
        todoService.createTodo(userId, request)

    @PatchMapping("/{todoId}/title")
    @ResponseStatus(OK)
    fun updateTitle(
        @PathVariable userId: Long,
        @PathVariable todoId: Long,
        @Valid @RequestBody request: UpdateTodoTitleRequest
    ): TodoResponse =
        todoService.updateTodoTitle(userId, todoId, request)

    @PatchMapping("/{todoId}/completed")
    @ResponseStatus(OK)
    fun updateCompletion(
        @PathVariable userId: Long,
        @PathVariable todoId: Long,
        @Valid @RequestBody request: UpdateTodoCompletionRequest
    ): TodoResponse =
        todoService.updateTodoCompletion(userId, todoId, request)
}
