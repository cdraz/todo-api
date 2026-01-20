package com.github.cdraz.todoapi.service

import com.github.cdraz.todoapi.dto.CreateTodoRequest
import com.github.cdraz.todoapi.entity.TodoEntity
import com.github.cdraz.todoapi.entity.UserEntity
import com.github.cdraz.todoapi.exception.UserNotFoundException
import com.github.cdraz.todoapi.repository.TodoRepository
import com.github.cdraz.todoapi.repository.UserRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Instant
import java.util.Optional

class TodoServiceTest : DescribeSpec({

    val todoRepository = mockk<TodoRepository>(relaxed = false)
    val userRepository = mockk<UserRepository>(relaxed = false)
    val todoService = TodoService(todoRepository, userRepository)

    afterTest {
        clearMocks(todoRepository, userRepository)
    }

    describe("createTodo") {

        it("creates a new todo when the user exists") {
            val userId = 1L
            val request = CreateTodoRequest(title = "Apply for jobs")

            val user = UserEntity(
                id = userId,
                email = "test@example.com",
                createdAt = Instant.now()
            )

            val savedTodo = TodoEntity(
                id = 10L,
                title = request.title,
                completed = false,
                createdAt = Instant.now(),
                user = user
            )

            every { userRepository.findById(userId) } returns Optional.of(user)
            every { todoRepository.save(any()) } returns savedTodo

            val result = todoService.createTodo(userId, request)

            result.id shouldBe 10L
            result.title shouldBe "Apply for jobs"
            result.completed shouldBe false
            result.createdAt shouldBe savedTodo.createdAt
            result.userId shouldBe userId

            verify(exactly = 1) { userRepository.findById(userId) }
            verify(exactly = 1) { todoRepository.save(any()) }
        }

        it("throws UserNotFoundException when the user does not exist") {
            val missingUserId = 999L
            val request = CreateTodoRequest(title = "This should fail")

            every { userRepository.findById(missingUserId) } returns Optional.empty()

            shouldThrow<UserNotFoundException> {
                todoService.createTodo(missingUserId, request)
            }

            verify(exactly = 1) { userRepository.findById(missingUserId) }
            verify(exactly = 0) { todoRepository.save(any()) }
        }
    }
})
