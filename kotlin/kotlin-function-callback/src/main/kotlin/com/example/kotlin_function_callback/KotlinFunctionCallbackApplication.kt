package com.example.kotlin_function_callback

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.model.function.FunctionCallback
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Description

@SpringBootApplication
class KotlinFunctionCallbackApplication {

	@Bean
	fun init(chatClientBuilder: ChatClient.Builder) = CommandLineRunner {
		try {
			val chatClient = chatClientBuilder.build();
			val response = chatClient
				.prompt("What are the weather conditions in San Francisco, Tokyo, and Paris? Find the temperature in Celsius for each of the three locations.")
				.functions("WeatherInfo")
				.call().chatResponse();

			println("Response: $response")
		} catch (e: Exception) {
			println("Error during weather check: ${e.message}")
			e.printStackTrace()
		}
	}
}

@Configuration
class Config {

	@Bean
	fun weatherFunctionInfo(currentWeather: (WeatherRequest) -> WeatherResponse): FunctionCallback {
		return FunctionCallback.builder()
			.function("WeatherInfo", currentWeather)
			.description(
				"Find the weather conditions, forecasts, and temperatures for a location, like a city or state."
			)
			.inputType(WeatherRequest::class.java)
			.build()
	}

	@Bean
	@Description("Get current weather")
	fun currentWeather(): (WeatherRequest) -> WeatherResponse = { request ->
		MockKotlinWeatherService().invoke(request)
	}
}

fun main(args: Array<String>) {
	runApplication<KotlinFunctionCallbackApplication>(*args)
}
