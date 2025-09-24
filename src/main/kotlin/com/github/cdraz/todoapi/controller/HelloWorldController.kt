package com.github.cdraz.todoapi.controller

import com.github.cdraz.todoapi.service.HelloWorldService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloWorldController(
    private val helloWorldService: HelloWorldService
) {
    @GetMapping("/hello")
    fun hello(): String = helloWorldService.getHelloWorld()
}
