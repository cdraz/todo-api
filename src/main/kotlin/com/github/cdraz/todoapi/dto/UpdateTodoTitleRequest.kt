package com.github.cdraz.todoapi.dto

import jakarta.validation.constraints.NotBlank

data class UpdateTodoTitleRequest(
    @field:NotBlank
    val title: String
)
