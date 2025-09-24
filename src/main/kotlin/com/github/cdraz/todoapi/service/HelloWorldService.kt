package com.github.cdraz.todoapi.service

import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory

@Service
class HelloWorldService {
    private val logger = LoggerFactory.getLogger(HelloWorldService::class.java)

    fun getHelloWorld(): String {
        logger.info("getHelloWorld called")
        return "Hello, World!"
    }
}
