package com.example.kotlin_hello_world

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.boot.CommandLineRunner
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@TestPropertySource(properties = [
    "spring.ai.openai.api-key=test-key-for-context-loading"
])
class KotlinHelloWorldApplicationTests {

	@MockitoBean
	private lateinit var jokeRunner: CommandLineRunner

	@Test
	fun contextLoads() {
		// Simple smoke test - application context should load without errors
		// This validates that all Spring AI configuration is properly set up
		// The CommandLineRunner is mocked to prevent actual API calls during testing
	}

}
