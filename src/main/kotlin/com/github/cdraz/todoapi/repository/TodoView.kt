package com.github.cdraz.todoapi.repository

import java.time.Instant

interface TodoView {
    val id: Long
    val title: String
    val completed: Boolean
    val createdAt: Instant
    val userId: Long
}
