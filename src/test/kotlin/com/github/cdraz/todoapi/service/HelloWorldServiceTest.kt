package com.github.cdraz.todoapi.service

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class HelloWorldServiceTest : StringSpec({
    "getHelloWorld should return Hello, World!" {
        val service = HelloWorldService()
        service.getHelloWorld() shouldBe "Hello, World!"
    }
})
