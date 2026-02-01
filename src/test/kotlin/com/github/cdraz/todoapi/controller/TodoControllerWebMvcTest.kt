package com.github.cdraz.todoapi.controller

import com.github.cdraz.todoapi.dto.CreateTodoRequest
import com.github.cdraz.todoapi.dto.TodoResponse
import com.github.cdraz.todoapi.dto.UpdateTodoCompletionRequest
import com.github.cdraz.todoapi.dto.UpdateTodoTitleRequest
import com.github.cdraz.todoapi.service.TodoService
import com.github.cdraz.todoapi.exception.TodoNotFoundForUserException
import com.github.cdraz.todoapi.exception.UserNotFoundException
import org.springframework.test.web.servlet.get
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import java.time.Instant

@WebMvcTest(TodoController::class)
class TodoControllerWebMvcTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var todoService: TodoService

    @Test
    fun `GET users userId todos returns 200 and list of todos`() {
        val userId = 1L
        val t1 = TodoResponse(
            id = 2L,
            title = "Newest",
            completed = false,
            createdAt = Instant.parse("2026-02-01T10:00:00Z"),
            userId = userId
        )
        val t2 = TodoResponse(
            id = 1L,
            title = "Older",
            completed = true,
            createdAt = Instant.parse("2026-02-01T09:00:00Z"),
            userId = userId
        )

        every { todoService.getTodosForUser(userId) } returns listOf(t1, t2)

        mockMvc.get("/users/$userId/todos")
            .andExpect {
                status { isOk() }
                jsonPath("$.length()") { value(2) }
                jsonPath("$[0].id") { value(2) }
                jsonPath("$[0].title") { value("Newest") }
                jsonPath("$[0].completed") { value(false) }
                jsonPath("$[0].userId") { value(1) }
                jsonPath("$[1].id") { value(1) }
                jsonPath("$[1].completed") { value(true) }
            }

        verify(exactly = 1) { todoService.getTodosForUser(userId) }
    }

    @Test
    fun `GET users userId todos returns 200 and empty list when no todos`() {
        val userId = 1L

        every { todoService.getTodosForUser(userId) } returns emptyList()

        mockMvc.get("/users/$userId/todos")
            .andExpect {
                status { isOk() }
                jsonPath("$.length()") { value(0) }
            }

        verify(exactly = 1) { todoService.getTodosForUser(userId) }
    }

    @Test
    fun `GET users userId todos todoId returns 200 and todo`() {
        val userId = 1L
        val todoId = 2L

        val response = TodoResponse(
            id = todoId,
            title = "Apply for jobs",
            completed = false,
            createdAt = Instant.parse("2026-02-01T10:00:00Z"),
            userId = userId
        )

        every { todoService.getTodo(userId, todoId) } returns response

        mockMvc.get("/users/$userId/todos/$todoId")
            .andExpect {
                status { isOk() }
                jsonPath("$.id") { value(2) }
                jsonPath("$.title") { value("Apply for jobs") }
                jsonPath("$.completed") { value(false) }
                jsonPath("$.userId") { value(1) }
            }

        verify(exactly = 1) { todoService.getTodo(userId, todoId) }
    }

    @Test
    fun `GET users userId todos returns 400 when userId is not a number`() {
        mockMvc.get("/users/abc/todos")
            .andExpect {
                status { isBadRequest() }
            }

        verify(exactly = 0) { todoService.getTodosForUser(any()) }
    }

    @Test
    fun `GET users userId todos todoId returns 400 when todoId is not a number`() {
        mockMvc.get("/users/1/todos/abc")
            .andExpect {
                status { isBadRequest() }
            }

        verify(exactly = 0) { todoService.getTodo(any(), any()) }
    }

    @Test
    fun `GET users userId todos returns 404 when user does not exist`() {
        val userId = 999L
        every { todoService.getTodosForUser(userId) } throws UserNotFoundException(userId)

        mockMvc.get("/users/$userId/todos")
            .andExpect {
                status { isNotFound() }
            }

        verify(exactly = 1) { todoService.getTodosForUser(userId) }
    }

    @Test
    fun `GET users userId todos todoId returns 404 when todo not found for user`() {
        val userId = 1L
        val todoId = 999L

        every { todoService.getTodo(userId, todoId) } throws TodoNotFoundForUserException(todoId, userId)

        mockMvc.get("/users/$userId/todos/$todoId")
            .andExpect {
                status { isNotFound() }
            }

        verify(exactly = 1) { todoService.getTodo(userId, todoId) }
    }

    @Test
    fun `POST users userId todos returns 201 when request is valid`() {
        val userId = 1L
        val title = "Apply for jobs"

        val response = TodoResponse(
            id = 10L,
            title = title,
            completed = false,
            createdAt = Instant.now(),
            userId = userId
        )

        every {
            todoService.createTodo(
                userId,
                match { it.title == title }
            )
        } returns response

        mockMvc.post("/users/$userId/todos") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"title":"$title"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { value(10) }
            jsonPath("$.title") { value(title) }
            jsonPath("$.completed") { value(false) }
            jsonPath("$.userId") { value(1) }
        }

        verify(exactly = 1) {
            todoService.createTodo(userId, match<CreateTodoRequest> { it.title == title })
        }
    }

    @Test
    fun `POST users userId todos returns 400 when title is blank`() {
        val userId = 1L

        mockMvc.post("/users/$userId/todos") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"title":""}"""
        }.andExpect {
            status { isBadRequest() }
        }

        verify(exactly = 0) { todoService.createTodo(any(), any()) }
    }

    @Test
    fun `POST users userId todos returns 400 when request body is missing`() {
        val userId = 1L

        mockMvc.post("/users/$userId/todos") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isBadRequest() }
        }

        verify(exactly = 0) { todoService.createTodo(any(), any()) }
    }

    @Test
    fun `PATCH users userId todos todoId title returns 200 when request is valid`() {
        val userId = 1L
        val todoId = 2L
        val newTitle = "Updated title"

        val response = TodoResponse(
            id = todoId,
            title = newTitle,
            completed = false,
            createdAt = Instant.now(),
            userId = userId
        )

        every {
            todoService.updateTodoTitle(
                userId,
                todoId,
                match { it.title == newTitle }
            )
        } returns response

        mockMvc.patch("/users/$userId/todos/$todoId/title") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"title":"$newTitle"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.id") { value(2) }
            jsonPath("$.title") { value(newTitle) }
            jsonPath("$.completed") { value(false) }
            jsonPath("$.userId") { value(1) }
        }

        verify(exactly = 1) {
            todoService.updateTodoTitle(
                userId,
                todoId,
                match<UpdateTodoTitleRequest> { it.title == newTitle }
            )
        }
    }

    @Test
    fun `PATCH users userId todos todoId title returns 400 when title is blank`() {
        val userId = 1L
        val todoId = 2L

        mockMvc.patch("/users/$userId/todos/$todoId/title") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"title":""}"""
        }.andExpect {
            status { isBadRequest() }
        }

        verify(exactly = 0) { todoService.updateTodoTitle(any(), any(), any()) }
    }

    @Test
    fun `PATCH users userId todos todoId title returns 400 when request body is missing`() {
        val userId = 1L
        val todoId = 2L

        mockMvc.patch("/users/$userId/todos/$todoId/title") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isBadRequest() }
        }

        verify(exactly = 0) { todoService.updateTodoTitle(any(), any(), any()) }
    }


    @Test
    fun `PATCH users userId todos todoId completed returns 200 when request is valid`() {
        val userId = 1L
        val todoId = 2L
        val completed = true

        val response = TodoResponse(
            id = todoId,
            title = "Apply for jobs",
            completed = completed,
            createdAt = Instant.now(),
            userId = userId
        )

        every {
            todoService.updateTodoCompletion(
                userId,
                todoId,
                match { it.completed == completed }
            )
        } returns response

        mockMvc.patch("/users/$userId/todos/$todoId/completed") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"completed":$completed}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.id") { value(2) }
            jsonPath("$.completed") { value(true) }
            jsonPath("$.userId") { value(1) }
        }

        verify(exactly = 1) {
            todoService.updateTodoCompletion(
                userId,
                todoId,
                match<UpdateTodoCompletionRequest> { it.completed == completed }
            )
        }
    }

    @Test
    fun `PATCH users userId todos todoId completed returns 400 when request body is missing`() {
        val userId = 1L
        val todoId = 2L

        mockMvc.patch("/users/$userId/todos/$todoId/completed") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isBadRequest() }
        }

        verify(exactly = 0) { todoService.updateTodoCompletion(any(), any(), any()) }
    }

    @Test
    fun `PATCH users userId todos todoId completed returns 400 when completed field is missing`() {
        val userId = 1L
        val todoId = 2L

        // JSON exists but missing the required boolean field -> binding error -> 400
        mockMvc.patch("/users/$userId/todos/$todoId/completed") {
            contentType = MediaType.APPLICATION_JSON
            content = """{}"""
        }.andExpect {
            status { isBadRequest() }
        }

        verify(exactly = 0) { todoService.updateTodoCompletion(any(), any(), any()) }
    }
}
