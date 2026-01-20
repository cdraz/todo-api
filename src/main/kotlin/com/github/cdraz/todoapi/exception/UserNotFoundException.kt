package com.github.cdraz.todoapi.exception

class UserNotFoundException(userId: Long) :
    RuntimeException("User with id $userId not found")
