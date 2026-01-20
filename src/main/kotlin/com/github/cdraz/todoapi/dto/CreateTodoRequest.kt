package com.github.cdraz.todoapi.dto

import jakarta.validation.constraints.NotBlank

data class CreateTodoRequest(
    @field:NotBlank
    val title: String
)
