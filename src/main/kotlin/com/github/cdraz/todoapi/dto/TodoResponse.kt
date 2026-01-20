package com.github.cdraz.todoapi.dto

import java.time.Instant

data class TodoResponse(
    val id: Long,
    val title: String,
    val completed: Boolean,
    val createdAt: Instant,
    val userId: Long,
)
