package com.github.cdraz.todoapi.service

import com.github.cdraz.todoapi.dto.CreateTodoRequest
import com.github.cdraz.todoapi.dto.UpdateTodoCompletionRequest
import com.github.cdraz.todoapi.dto.UpdateTodoTitleRequest
import com.github.cdraz.todoapi.entity.TodoEntity
import com.github.cdraz.todoapi.entity.UserEntity
import com.github.cdraz.todoapi.exception.TodoNotFoundForUserException
import com.github.cdraz.todoapi.exception.UserNotFoundException
import com.github.cdraz.todoapi.repository.TodoRepository
import com.github.cdraz.todoapi.repository.UserRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.confirmVerified
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

    describe("updateTodoTitle") {

        it("updates the title when todo exists for the user") {
            val userId = 1L
            val todoId = 10L
            val oldTitle = "Old title"
            val newTitle = "New title"

            val user = UserEntity(
                id = userId,
                email = "test@example.com",
                createdAt = Instant.now()
            )

            val todo = TodoEntity(
                id = todoId,
                title = oldTitle,
                completed = false,
                createdAt = Instant.now(),
                user = user
            )

            every { todoRepository.findByIdAndUser_Id(todoId, userId) } returns todo

            val result = todoService.updateTodoTitle(
                userId = userId,
                todoId = todoId,
                request = UpdateTodoTitleRequest(title = newTitle)
            )

            // entity mutated
            todo.title shouldBe newTitle

            // response reflects updated entity
            result.id shouldBe todoId
            result.title shouldBe newTitle
            result.completed shouldBe false
            result.createdAt shouldBe todo.createdAt
            result.userId shouldBe userId

            verify(exactly = 1) { todoRepository.findByIdAndUser_Id(todoId, userId) }
            verify(exactly = 0) { todoRepository.save(any()) }
            confirmVerified(todoRepository)
        }

        it("does not change the title when the new title is the same") {
            val userId = 1L
            val todoId = 10L
            val sameTitle = "Same title"

            val user = UserEntity(
                id = userId,
                email = "test@example.com",
                createdAt = Instant.now()
            )

            val todo = TodoEntity(
                id = todoId,
                title = sameTitle,
                completed = false,
                createdAt = Instant.now(),
                user = user
            )

            every { todoRepository.findByIdAndUser_Id(todoId, userId) } returns todo

            val result = todoService.updateTodoTitle(
                userId = userId,
                todoId = todoId,
                request = UpdateTodoTitleRequest(title = sameTitle)
            )

            todo.title shouldBe sameTitle
            result.title shouldBe sameTitle

            verify(exactly = 1) { todoRepository.findByIdAndUser_Id(todoId, userId) }
            verify(exactly = 0) { todoRepository.save(any()) }
            confirmVerified(todoRepository)
        }

        it("throws TodoNotFoundForUserException when todo does not exist for the user") {
            val userId = 1L
            val todoId = 999L

            every { todoRepository.findByIdAndUser_Id(todoId, userId) } returns null

            shouldThrow<TodoNotFoundForUserException> {
                todoService.updateTodoTitle(
                    userId = userId,
                    todoId = todoId,
                    request = UpdateTodoTitleRequest(title = "doesn't matter")
                )
            }

            verify(exactly = 1) { todoRepository.findByIdAndUser_Id(todoId, userId) }
            verify(exactly = 0) { todoRepository.save(any()) }
            confirmVerified(todoRepository)
        }
    }

    describe("updateTodoCompletion") {

        it("updates completion when todo exists for the user") {
            val userId = 1L
            val todoId = 10L

            val user = UserEntity(
                id = userId,
                email = "test@example.com",
                createdAt = Instant.now()
            )

            val todo = TodoEntity(
                id = todoId,
                title = "Apply for jobs",
                completed = false,
                createdAt = Instant.now(),
                user = user
            )

            every { todoRepository.findByIdAndUser_Id(todoId, userId) } returns todo

            val result = todoService.updateTodoCompletion(
                userId = userId,
                todoId = todoId,
                request = UpdateTodoCompletionRequest(completed = true)
            )

            todo.completed shouldBe true

            result.id shouldBe todoId
            result.title shouldBe "Apply for jobs"
            result.completed shouldBe true
            result.createdAt shouldBe todo.createdAt
            result.userId shouldBe userId

            verify(exactly = 1) { todoRepository.findByIdAndUser_Id(todoId, userId) }
            verify(exactly = 0) { todoRepository.save(any()) }
            confirmVerified(todoRepository)
        }

        it("does not change completion when the value is the same") {
            val userId = 1L
            val todoId = 10L

            val user = UserEntity(
                id = userId,
                email = "test@example.com",
                createdAt = Instant.now()
            )

            val todo = TodoEntity(
                id = todoId,
                title = "Apply for jobs",
                completed = true,
                createdAt = Instant.now(),
                user = user
            )

            every { todoRepository.findByIdAndUser_Id(todoId, userId) } returns todo

            val result = todoService.updateTodoCompletion(
                userId = userId,
                todoId = todoId,
                request = UpdateTodoCompletionRequest(completed = true)
            )

            todo.completed shouldBe true
            result.completed shouldBe true

            verify(exactly = 1) { todoRepository.findByIdAndUser_Id(todoId, userId) }
            verify(exactly = 0) { todoRepository.save(any()) }
            confirmVerified(todoRepository)
        }

        it("throws TodoNotFoundForUserException when todo does not exist for the user") {
            val userId = 1L
            val todoId = 999L

            every { todoRepository.findByIdAndUser_Id(todoId, userId) } returns null

            shouldThrow<TodoNotFoundForUserException> {
                todoService.updateTodoCompletion(
                    userId = userId,
                    todoId = todoId,
                    request = UpdateTodoCompletionRequest(completed = true)
                )
            }

            verify(exactly = 1) { todoRepository.findByIdAndUser_Id(todoId, userId) }
            verify(exactly = 0) { todoRepository.save(any()) }
            confirmVerified(todoRepository)
        }
    }
})
