package com.example.kotlin_function_callback

import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyDescription

class MockKotlinWeatherService : Function1<WeatherRequest, WeatherResponse> {

    override fun invoke(weatherRequest: WeatherRequest): WeatherResponse {
        var temperature = 10.0
        if (weatherRequest.location.contains("Paris")) {
            temperature = 15.0
        }
        else if (weatherRequest.location.contains("Tokyo")) {
            temperature = 10.0
        }
        else if (weatherRequest.location.contains("San Francisco")) {
            temperature = 30.0
        }

        return WeatherResponse(temperature, 15.0, 20.0, 2.0, 53, 45, Unit.C);
    }
}

/**
 * Temperature units.
 */
enum class Unit(val unitName: String) {

    /**
     * Celsius.
     */
    C("metric"),
    /**
     * Fahrenheit.
     */
    F("imperial");
}

/**
 * Weather Function request.
 */
@JsonInclude(Include.NON_NULL)
@JsonClassDescription("Weather API request")
data class WeatherRequest(
    @get:JsonPropertyDescription("The city and state e.g. San Francisco, CA")
    val location: String,

    @get:JsonPropertyDescription("The city latitude")
    val lat: Double,

    @get:JsonPropertyDescription("The city longitude")
    val lon: Double,

    @get:JsonPropertyDescription("Temperature unit")
    val unit: Unit
)


/**
 * Weather Function response.
 */
data class WeatherResponse(val temp: Double,
                           val feels_like: Double,
                           val temp_min: Double,
                           val temp_max: Double,
                           val pressure: Int,
                           val humidity: Int,
                           val unit: Unit
)

