package com.github.cdraz.todoapi.controller

import com.github.cdraz.todoapi.dto.CreateUserRequest
import com.github.cdraz.todoapi.dto.UserResponse
import com.github.cdraz.todoapi.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus.CREATED
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService
) {
    @PostMapping
    @ResponseStatus(CREATED)
    fun createUser(
        @Valid @RequestBody request: CreateUserRequest
    ): UserResponse =
        userService.createUser(request)
}
