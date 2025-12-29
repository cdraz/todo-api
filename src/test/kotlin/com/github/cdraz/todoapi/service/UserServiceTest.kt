package com.github.cdraz.todoapi.service

import com.github.cdraz.todoapi.dto.CreateUserRequest
import com.github.cdraz.todoapi.entity.UserEntity
import com.github.cdraz.todoapi.exception.EmailAlreadyExistsException
import com.github.cdraz.todoapi.repository.UserRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Instant

class UserServiceTest : DescribeSpec({

    val userRepository = mockk<UserRepository>(relaxed = false)
    val userService = UserService(userRepository)

    afterTest {
        clearMocks(userRepository)
    }

    describe("createUser") {

        it("creates a new user when email does not already exist") {
            val request = CreateUserRequest(email = "test@example.com")

            val savedEntity = UserEntity(
                id = 1L,
                email = request.email,
                createdAt = Instant.now()
            )

            every { userRepository.existsByEmail(eq("test@example.com")) } returns false
            every { userRepository.save(any()) } returns savedEntity

            val result = userService.createUser(request)

            result.id shouldBe 1L
            result.email shouldBe "test@example.com"
            result.createdAt shouldBe savedEntity.createdAt

            verify(exactly = 1) { userRepository.existsByEmail("test@example.com") }
            verify(exactly = 1) { userRepository.save(any()) }
        }

        it("throws EmailAlreadyExistsException when email already exists") {
            val request = CreateUserRequest(email = "duplicate@example.com")

            every { userRepository.existsByEmail(eq("duplicate@example.com")) } returns true

            shouldThrow<EmailAlreadyExistsException> {
                userService.createUser(request)
            }

            verify(exactly = 1) { userRepository.existsByEmail("duplicate@example.com") }
            verify(exactly = 0) { userRepository.save(any()) }
        }
    }
})
