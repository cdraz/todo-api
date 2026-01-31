package com.github.cdraz.todoapi.controller

import com.github.cdraz.todoapi.dto.CreateTodoRequest
import com.github.cdraz.todoapi.dto.TodoResponse
import com.github.cdraz.todoapi.service.TodoService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.time.Instant

@WebMvcTest(TodoController::class)
class TodoControllerWebMvcTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var todoService: TodoService

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
    fun `PATCH users userId todos todoId title returns 400 when request body is missing`() {
        val userId = 1L
        val todoId = 1

        mockMvc.post("/users/$userId/todos/$todoId/title") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isBadRequest() }
        }

        verify(exactly = 0) { todoService.createTodo(any(), any()) }
    }
}
