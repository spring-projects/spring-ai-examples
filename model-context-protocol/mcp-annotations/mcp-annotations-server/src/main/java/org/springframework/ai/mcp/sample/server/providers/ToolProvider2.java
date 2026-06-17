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
package org.springframework.ai.mcp.sample.server.providers;

import java.util.List;
import java.util.Map;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;
import io.modelcontextprotocol.spec.McpSchema.ElicitResult;
import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import io.modelcontextprotocol.spec.McpSchema.ModelHint;
import io.modelcontextprotocol.spec.McpSchema.ModelPreferences;
import io.modelcontextprotocol.spec.McpSchema.ProgressNotification;

import org.springframework.ai.mcp.annotation.McpProgressToken;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;

/**
 * @author Christian Tzolov
 */
// @Service
public class ToolProvider2 {

	@McpTool(description = "Test tool", name = "tool1", generateOutputSchema = true)
	public String toolLggingSamplingElicitationProgress(McpSyncServerExchange exchange, @McpToolParam String input,
			@McpProgressToken String progressToken) {

		exchange.loggingNotification(LoggingMessageNotification.builder(LoggingLevel.INFO, "Tool1 Started!").build());

		exchange.progressNotification(
				ProgressNotification.builder(progressToken, 0.0).total(1.0).message("tool call start").build());

		exchange.ping(); // call client ping

		// call elicitation
		var elicitationRequest = McpSchema.ElicitFormRequest.builder("Test message", Map.of("type", "object",
				"properties", Map.of("name", Map.of("type", "string"), "age", Map.of("type", "integer"))))
			.build();

		ElicitResult elicitationResult = exchange.createElicitation(elicitationRequest);

		exchange.progressNotification(
				ProgressNotification.builder(progressToken, 0.50).total(1.0).message("elicitation completed").build());

		// call sampling
		var createMessageRequest = McpSchema.CreateMessageRequest
			.builder(List.of(new McpSchema.SamplingMessage(McpSchema.Role.USER,
					TextContent.builder("Test Sampling Message").build())), 2000)
			.modelPreferences(ModelPreferences.builder()
				.hints(List.of(ModelHint.of("OpenAi"), ModelHint.of("Ollama")))
				.costPriority(1.0)
				.speedPriority(1.0)
				.intelligencePriority(1.0)
				.build())
			.build();

		CreateMessageResult samplingResponse = exchange.createMessage(createMessageRequest);

		exchange.progressNotification(
				ProgressNotification.builder(progressToken, 1.0).total(1.0).message("sampling completed").build());

		exchange.loggingNotification(LoggingMessageNotification.builder(LoggingLevel.INFO, "Tool1 Done!").build());

		return "CALL RESPONSE: " + samplingResponse.toString() + ", " + elicitationResult.toString();
	}

}
