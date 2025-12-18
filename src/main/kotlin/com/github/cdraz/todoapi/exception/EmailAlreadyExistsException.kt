package com.github.cdraz.todoapi.exception

class EmailAlreadyExistsException(email: String) :
    RuntimeException("User with email '$email' already exists")
