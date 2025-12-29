package com.github.cdraz.todoapi.controller

import com.github.cdraz.todoapi.dto.UserResponse
import com.github.cdraz.todoapi.service.UserService
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

@WebMvcTest(UserController::class)
class UserControllerWebMvcTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var userService: UserService

    @Test
    fun `POST users returns 201 when request is valid`() {
        val requestEmail = "test@example.com"

        val response = UserResponse(
            id = 1L,
            email = requestEmail,
            createdAt = Instant.now()
        )

        every { userService.createUser(match { it.email == requestEmail }) } returns response

        mockMvc.post("/users") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"$requestEmail"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { value(1) }
            jsonPath("$.email") { value(requestEmail) }
        }

        verify(exactly = 1) { userService.createUser(match { it.email == requestEmail }) }
    }

    @Test
    fun `POST users returns 400 when email is blank`() {
        mockMvc.post("/users") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":""}"""
        }.andExpect {
            status { isBadRequest() }
        }

        verify(exactly = 0) { userService.createUser(any()) }
    }

    @Test
    fun `POST users returns 400 when request body is missing`() {
        mockMvc.post("/users") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isBadRequest() }
        }

        verify(exactly = 0) { userService.createUser(any()) }
    }
}
