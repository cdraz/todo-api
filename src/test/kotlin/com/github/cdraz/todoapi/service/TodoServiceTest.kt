package com.github.cdraz.todoapi.service

import com.github.cdraz.todoapi.dto.CreateTodoRequest
import com.github.cdraz.todoapi.dto.TodoResponse
import com.github.cdraz.todoapi.dto.UpdateTodoCompletionRequest
import com.github.cdraz.todoapi.dto.UpdateTodoTitleRequest
import com.github.cdraz.todoapi.entity.TodoEntity
import com.github.cdraz.todoapi.entity.UserEntity
import com.github.cdraz.todoapi.exception.TodoNotFoundForUserException
import com.github.cdraz.todoapi.exception.UserNotFoundException
import com.github.cdraz.todoapi.repository.TodoRepository
import com.github.cdraz.todoapi.repository.TodoView
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

    describe("getTodo") {

        it("returns TodoResponse when todo exists for the user (does not check user existence)") {
            val userId = 1L
            val todoId = 10L
            val createdAt = Instant.parse("2026-02-01T10:00:00Z")

            val todoView = mockk<TodoView>(relaxed = false)
            every { todoView.id } returns todoId
            every { todoView.title } returns "Apply for jobs"
            every { todoView.completed } returns false
            every { todoView.createdAt } returns createdAt
            every { todoView.userId } returns userId

            every { todoRepository.findViewByIdAndUserId(todoId, userId) } returns todoView

            val result = todoService.getTodo(userId, todoId)

            result shouldBe TodoResponse(
                id = todoId,
                title = "Apply for jobs",
                completed = false,
                createdAt = createdAt,
                userId = userId
            )

            verify(exactly = 1) { todoRepository.findViewByIdAndUserId(todoId, userId) }
            verify(exactly = 0) { userRepository.existsById(any()) }
            confirmVerified(todoRepository, userRepository)
        }

        it("throws UserNotFoundException when todo is not found and user does not exist") {
            val userId = 999L
            val todoId = 10L

            every { todoRepository.findViewByIdAndUserId(todoId, userId) } returns null
            every { userRepository.existsById(userId) } returns false

            shouldThrow<UserNotFoundException> {
                todoService.getTodo(userId, todoId)
            }

            verify(exactly = 1) { todoRepository.findViewByIdAndUserId(todoId, userId) }
            verify(exactly = 1) { userRepository.existsById(userId) }
            confirmVerified(todoRepository, userRepository)
        }

        it("throws TodoNotFoundForUserException when todo is not found but user exists") {
            val userId = 1L
            val todoId = 999L

            every { todoRepository.findViewByIdAndUserId(todoId, userId) } returns null
            every { userRepository.existsById(userId) } returns true

            shouldThrow<TodoNotFoundForUserException> {
                todoService.getTodo(userId, todoId)
            }

            verify(exactly = 1) { todoRepository.findViewByIdAndUserId(todoId, userId) }
            verify(exactly = 1) { userRepository.existsById(userId) }
            confirmVerified(todoRepository, userRepository)
        }
    }

    describe("getTodosForUser") {

        it("returns todos for existing user ordered by createdAt desc") {
            val userId = 1L
            val createdAt1 = Instant.parse("2026-02-01T10:00:00Z")
            val createdAt2 = Instant.parse("2026-02-01T09:00:00Z")

            val v1 = mockk<TodoView>(relaxed = false)
            every { v1.id } returns 2L
            every { v1.title } returns "Newest"
            every { v1.completed } returns false
            every { v1.createdAt } returns createdAt1
            every { v1.userId } returns userId

            val v2 = mockk<TodoView>(relaxed = false)
            every { v2.id } returns 1L
            every { v2.title } returns "Older"
            every { v2.completed } returns true
            every { v2.createdAt } returns createdAt2
            every { v2.userId } returns userId

            every { userRepository.existsById(userId) } returns true
            every { todoRepository.findAllProjectedByUser_IdOrderByCreatedAtDesc(userId) } returns listOf(v1, v2)

            val result = todoService.getTodosForUser(userId)

            result shouldBe listOf(
                TodoResponse(
                    id = 2L,
                    title = "Newest",
                    completed = false,
                    createdAt = createdAt1,
                    userId = userId
                ),
                TodoResponse(
                    id = 1L,
                    title = "Older",
                    completed = true,
                    createdAt = createdAt2,
                    userId = userId
                )
            )

            verify(exactly = 1) { userRepository.existsById(userId) }
            verify(exactly = 1) { todoRepository.findAllProjectedByUser_IdOrderByCreatedAtDesc(userId) }
            confirmVerified(todoRepository, userRepository)
        }

        it("returns an empty list when user exists but has no todos") {
            val userId = 1L

            every { userRepository.existsById(userId) } returns true
            every { todoRepository.findAllProjectedByUser_IdOrderByCreatedAtDesc(userId) } returns emptyList()

            val result = todoService.getTodosForUser(userId)

            result shouldBe emptyList()

            verify(exactly = 1) { userRepository.existsById(userId) }
            verify(exactly = 1) { todoRepository.findAllProjectedByUser_IdOrderByCreatedAtDesc(userId) }
            confirmVerified(todoRepository, userRepository)
        }

        it("throws UserNotFoundException when user does not exist (does not query todos)") {
            val userId = 999L

            every { userRepository.existsById(userId) } returns false

            shouldThrow<UserNotFoundException> {
                todoService.getTodosForUser(userId)
            }

            verify(exactly = 1) { userRepository.existsById(userId) }
            verify(exactly = 0) { todoRepository.findAllProjectedByUser_IdOrderByCreatedAtDesc(any()) }
            confirmVerified(todoRepository, userRepository)
        }
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
