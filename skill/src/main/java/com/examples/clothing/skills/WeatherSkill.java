/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.examples.clothing.skills;

import org.springframework.ai.skill.annotation.Skill;
import org.springframework.ai.skill.annotation.SkillContent;
import org.springframework.ai.skill.annotation.SkillInit;
import org.springframework.ai.skill.annotation.SkillTools;
import java.util.List;
import java.util.Random;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.Tool;

/**
 * Weather Skill - Annotation-Based Example
 *
 * <p>Demonstrates how to implement a Skill using annotation mode:
 * <ul>
 *   <li>Pure POJO class, no interface implementation required</li>
 *   <li>Use @Skill to annotate the class</li>
 *   <li>Use @SkillContent to annotate the content method</li>
 *   <li>Use @SkillTools to annotate the tools method</li>
 *   <li>Framework automatically wraps it as SkillProxy</li>
 * </ul>
 *
 * <p><b>Use Cases</b>: General users, standard scenarios, rapid development
 *
 * <p><b>Notes</b>:
 * <ul>
 *   <li>Method names can be freely named (doesn't have to be getContent)</li>
 *   <li>Framework identifies methods through annotations and invokes via reflection</li>
 *   <li>Slightly slower than interface mode (reflection invocation), but fast enough for most scenarios</li>
 * </ul>
 *
 * @author Semir
 */
@Skill(
        name = "weather",
        description = "Provides weather information for cities around the world",
        source = "example",
        extensions = {"version=1.0.0", "author=Semir", "category=information"})
public class WeatherSkill {

    private final Random random = new Random();

    // Simulated weather conditions
    private static final String[] WEATHER_CONDITIONS = {
        "Sunny", "Cloudy", "Rainy", "Snowy", "Windy", "Foggy", "Partly Cloudy"
    };

    /**
     * Private constructor
     */
    private WeatherSkill() {}

    /**
     * Skill initialization method - Factory method
     *
     * <p>Used for lazy loading to create WeatherSkill instance
     */
    @SkillInit
    public static WeatherSkill create() {
        return new WeatherSkill();
    }

    /**
     * Method that returns Skill content
     *
     * <p>Method name can be freely named, framework identifies it through @SkillContent annotation
     */
    @SkillContent
    public String content() {
        return """
            # Weather Skill

            Provides weather information for cities around the world.

            ## Features

            - Get current weather for any city
            - Temperature in Celsius
            - Weather conditions (Sunny, Rainy, Cloudy, etc.)

            ## Available Tools

            - `getWeather(city)` - Get current weather for a specific city

            ## Usage

            Ask me "What's the weather in Beijing?" or "Tell me the weather in New York".
            """;
    }

    /**
     * Method that returns the tool list
     *
     * <p>Returns available tool callbacks, using Spring AI's ToolCallbacks utility class
     */
    @SkillTools
    public List<ToolCallback> tools() {
        // Use Spring AI's ToolCallbacks.from() to wrap @Tool annotated methods into tools
        return List.of(ToolCallbacks.from(this));
    }

    /**
     * Get weather information for a specified city (simulated data)
     *
     * <p>This is the actual tool method called by AI, annotated with @Tool
     *
     * @param city City name
     * @return Weather information as JSON string
     */
    @Tool(
            description =
                    "Get current weather information for a specific city. Returns temperature in Celsius, weather condition, humidity percentage, and wind speed in km/h.")
    public String getWeather(String city) {
        if (city == null || city.trim().isEmpty()) {
            return "{\"error\": \"City name is required\"}";
        }

        // Generate random temperature (-10 to 40 Celsius)
        int temperature = random.nextInt(51) - 10;

        // Randomly select weather condition
        String condition = WEATHER_CONDITIONS[random.nextInt(WEATHER_CONDITIONS.length)];

        // Generate random humidity (30% - 90%)
        int humidity = random.nextInt(61) + 30;

        // Generate random wind speed (0 - 30 km/h)
        int windSpeed = random.nextInt(31);

        // Return weather information in JSON format
        return String.format(
                """
            {
              "city": "%s",
              "temperature": %d,
              "unit": "Celsius",
              "condition": "%s",
              "humidity": %d,
              "windSpeed": %d,
              "windUnit": "km/h",
              "description": "The weather in %s is %s with a temperature of %d°C, humidity at %d%%, and wind speed of %d km/h."
            }
            """,
                city, temperature, condition, humidity, windSpeed, city, condition, temperature, humidity, windSpeed);
    }
}
