package com.github.cdraz.todoapi.exception

import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.http.converter.HttpMessageNotReadableException

@RestControllerAdvice
class GlobalExceptionHandler {

    // Handles validation failures on @Valid @RequestBody DTOs (e.g. invalid JSON fields)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException
    ): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors
            .associate { it.field to (it.defaultMessage ?: "Invalid value") }

        return ResponseEntity.badRequest().body(
            ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Validation failed",
                message = "One or more request fields are invalid",
                details = errors
            )
        )
    }

    // Handles validation failures on path variables, request params, and service-layer validation
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(
        ex: ConstraintViolationException
    ): ResponseEntity<ErrorResponse> {
        val errors = ex.constraintViolations
            .associate { violation ->
                violation.propertyPath.toString() to violation.message
            }

        return ResponseEntity.badRequest().body(
            ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Constraint violation",
                message = "One or more constraints were violated",
                details = errors
            )
        )
    }

    // Handles attempts to create a user with an email that already exists
    @ExceptionHandler(EmailAlreadyExistsException::class)
    fun handleEmailAlreadyExists(
        ex: EmailAlreadyExistsException
    ): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            ErrorResponse(
                status = HttpStatus.CONFLICT.value(),
                error = "Email already exists",
                message = ex.message
            )
        )
    }

    // Handles malformed or unreadable JSON in the request body
    @ExceptionHandler(HttpMessageNotReadableException::class)
    @Suppress("UnusedParameter") // Surpressing unused parameter for detekt
    fun handleUnreadableMessage(
        ex: HttpMessageNotReadableException
    ): ResponseEntity<ErrorResponse> {
        return ResponseEntity.badRequest().body(
            ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Malformed request",
                message = "Request body is missing or invalid JSON"
            )
        )
    }

    // Fallback handler for any unhandled exceptions (internal server errors)
    @ExceptionHandler(Exception::class)
    @Suppress("UnusedParameter") // Surpressing unused parameter for detekt
    fun handleGenericException(
        ex: Exception
    ): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ErrorResponse(
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                error = "Internal server error",
                message = "An unexpected error occurred"
            )
        )
    }
}
