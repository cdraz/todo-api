package com.github.cdraz.todoapi.service

import com.github.cdraz.todoapi.dto.CreateUserRequest
import com.github.cdraz.todoapi.dto.UserResponse
import com.github.cdraz.todoapi.entity.UserEntity
import com.github.cdraz.todoapi.exception.EmailAlreadyExistsException
import com.github.cdraz.todoapi.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) {
    @Transactional
    fun createUser(request: CreateUserRequest): UserResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw EmailAlreadyExistsException(request.email)
        }

        val user = UserEntity(
            email = request.email
        )

        val savedUser = userRepository.save(user)

        return UserResponse(
            id = savedUser.id,
            email = savedUser.email,
            createdAt = savedUser.createdAt
        )
    }
}
