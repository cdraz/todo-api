package com.github.cdraz.todoapi.exception

class TodoNotFoundForUserException(todoId: Long, userId: Long):
    RuntimeException("Todo $todoId not found for user $userId")
