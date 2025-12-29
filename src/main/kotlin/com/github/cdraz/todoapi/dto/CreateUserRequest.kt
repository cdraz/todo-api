package com.github.cdraz.todoapi.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class CreateUserRequest(
    @field:Email
    @field:NotBlank
    val email: String
)
