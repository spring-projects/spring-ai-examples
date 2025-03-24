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

import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;
import io.modelcontextprotocol.spec.McpSchema.ModelPreferences;
import org.slf4j.Logger;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * @author Christian Tzolov
 */
@Service
public class WeatherService {

	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(WeatherService.class);

	private final RestClient restClient;

	public WeatherService() {
		this.restClient = RestClient.create();
	}

	/**
	 * The response format from the Open-Meteo API
	 */
	public record WeatherResponse(Current current) {
		public record Current(LocalDateTime time, int interval, double temperature_2m) {
		}
	}

	@Tool(description = "Get the temperature (in celsius) for a specific location")
	public String getTemperature(@ToolParam(description = "The location latitude") double latitude,
			@ToolParam(description = "The location longitude") double longitude,
			ToolContext toolContext) {

		WeatherResponse weatherResponse = restClient
				.get()
				.uri("https://api.open-meteo.com/v1/forecast?latitude={latitude}&longitude={longitude}&current=temperature_2m",
						latitude, longitude)
				.retrieve()
				.body(WeatherResponse.class);

		String responseWithPoems = callMcpSampling(toolContext, weatherResponse);

		return responseWithPoems;
	}

	public String callMcpSampling(ToolContext toolContext, WeatherResponse weatherResponse) {

		String openAiWeatherPoem = "<no OpenAI poem>";
		String anthropicWeatherPoem = "<no Anthropic poem>";

		if (toolContext != null && toolContext.getContext().containsKey("exchange")) {

			// Spring AI MCP Auto-configuration injects the McpSyncServerExchange into the ToolContext under the key "exchange"
			McpSyncServerExchange exchange = (McpSyncServerExchange) toolContext.getContext().get("exchange");
			if (exchange.getClientCapabilities().sampling() != null) {
				var messageRequestBuilder = McpSchema.CreateMessageRequest.builder()
						.systemPrompt("You are a poet!")
						.messages(List.of(new McpSchema.SamplingMessage(McpSchema.Role.USER,
								new McpSchema.TextContent(
										"Please write a poem about thius weather forecast (temperature is in Celsious). Use markdown format :\n "
												+ ModelOptionsUtils.toJsonStringPrettyPrinter(weatherResponse)))));

				var opeAiLlmMessageRequest = messageRequestBuilder
						.modelPreferences(ModelPreferences.builder().addHint("openai").build())
						.build();
				CreateMessageResult openAiLlmResponse = exchange.createMessage(opeAiLlmMessageRequest);

				openAiWeatherPoem = ((McpSchema.TextContent) openAiLlmResponse.content()).text();

				var anthropicLlmMessageRequest = messageRequestBuilder
						.modelPreferences(ModelPreferences.builder().addHint("anthropic").build())
						.build();
				CreateMessageResult anthropicAiLlmResponse = exchange.createMessage(anthropicLlmMessageRequest);

				anthropicWeatherPoem = ((McpSchema.TextContent) anthropicAiLlmResponse.content()).text();

			}
		}

		String responseWithPoems = "OpenAI poem about the weather: " + openAiWeatherPoem + "\n\n" +
				"Anthropic poem about the weather: " + anthropicWeatherPoem + "\n"
				+ ModelOptionsUtils.toJsonStringPrettyPrinter(weatherResponse);

		logger.info(anthropicWeatherPoem, responseWithPoems);

		return responseWithPoems;

	}

}