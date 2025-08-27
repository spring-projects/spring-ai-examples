/* 
* Copyright 2025 - 2025 the original author or authors.
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
* https://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.springframework.ai.mcp.sample.server;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;
import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import io.modelcontextprotocol.spec.McpSchema.ModelPreferences;

/**
 * @author Christian Tzolov
 */
@Service
public class WeatherService {

	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(WeatherService.class);

	private final RestClient restClient = RestClient.create();

	/**
	 * The response format from the Open-Meteo API
	 */
	public record WeatherResponse(Current current) {
		public record Current(LocalDateTime time, int interval, double temperature_2m) {
		}
	}

	@McpTool(description = "Get the temperature (in celsius) for a specific location")
	public String getTemperature2(McpSyncServerExchange exchange,
			@McpToolParam(description = "The location latitude") double latitude,
			@McpToolParam(description = "The location longitude") double longitude) {

		WeatherResponse weatherResponse = restClient
				.get()
				.uri("https://api.open-meteo.com/v1/forecast?latitude={latitude}&longitude={longitude}&current=temperature_2m",
						latitude, longitude)
				.retrieve()
				.body(WeatherResponse.class);

		StringBuilder openAiWeatherPoem = new StringBuilder();
		StringBuilder anthropicWeatherPoem = new StringBuilder();

		exchange.loggingNotification(LoggingMessageNotification.builder()
				.level(LoggingLevel.INFO)
				.data("Start sampling")
				.build());

		if (exchange.getClientCapabilities().sampling() != null) {
			var messageRequestBuilder = McpSchema.CreateMessageRequest.builder()
					.systemPrompt("You are a poet!")
					.messages(List.of(new McpSchema.SamplingMessage(McpSchema.Role.USER,
							new McpSchema.TextContent(
									"Please write a poem about this weather forecast (temperature is in Celsius). Use markdown format :\n "
											+ ModelOptionsUtils
													.toJsonStringPrettyPrinter(weatherResponse)))));

			var opeAiLlmMessageRequest = messageRequestBuilder
					.modelPreferences(ModelPreferences.builder().addHint("openai").build())
					.build();
			CreateMessageResult openAiLlmResponse = exchange.createMessage(opeAiLlmMessageRequest);

			openAiWeatherPoem.append(((McpSchema.TextContent) openAiLlmResponse.content()).text());

			var anthropicLlmMessageRequest = messageRequestBuilder
					.modelPreferences(ModelPreferences.builder().addHint("anthropic").build())
					.build();
			CreateMessageResult anthropicAiLlmResponse = exchange.createMessage(anthropicLlmMessageRequest);

			anthropicWeatherPoem.append(((McpSchema.TextContent) anthropicAiLlmResponse.content()).text());

		}

		exchange.loggingNotification(LoggingMessageNotification.builder()
				.level(LoggingLevel.INFO)
				.data("Finish Sampling")
				.build());

		String responseWithPoems = "OpenAI poem about the weather: " + openAiWeatherPoem.toString() + "\n\n" +
				"Anthropic poem about the weather: " + anthropicWeatherPoem.toString() + "\n"
				+ ModelOptionsUtils.toJsonStringPrettyPrinter(weatherResponse);

		logger.info(anthropicWeatherPoem.toString(), responseWithPoems.toString());

		return responseWithPoems;

	}
}