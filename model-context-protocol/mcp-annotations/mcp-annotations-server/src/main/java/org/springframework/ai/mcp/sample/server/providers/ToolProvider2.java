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

import org.springaicommunity.mcp.annotation.McpProgressToken;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;
import io.modelcontextprotocol.spec.McpSchema.ElicitResult;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import io.modelcontextprotocol.spec.McpSchema.ModelHint;
import io.modelcontextprotocol.spec.McpSchema.ModelPreferences;
import io.modelcontextprotocol.spec.McpSchema.ProgressNotification;

/**
 * @author Christian Tzolov
 */
@Service
public class ToolProvider2 {

	@McpTool(description = "Test tool", name = "tool1")
	public String toolLggingSamplingElicitationProgress(McpSyncServerExchange exchange, @McpToolParam String input,
			@McpProgressToken String progressToken) {

		exchange.loggingNotification(LoggingMessageNotification.builder().data("Tool1 Started!").build());

		exchange.progressNotification(
				new ProgressNotification(progressToken, 0.0, 1.0, "tool call start"));

		exchange.ping(); // call client ping

		// call elicitation
		var elicitationRequest = McpSchema.ElicitRequest.builder()
				.message("Test message")
				.requestedSchema(
						Map.of("type", "object", "properties", Map.of("message", Map.of("type", "string"))))
				.build();

		ElicitResult elicitationResult = exchange.createElicitation(elicitationRequest);

		exchange.progressNotification(
				new ProgressNotification(progressToken, 0.50, 1.0, "elicitation completed"));

		// call sampling
		var createMessageRequest = McpSchema.CreateMessageRequest.builder()
				.messages(List.of(new McpSchema.SamplingMessage(McpSchema.Role.USER,
						new McpSchema.TextContent("Test Sampling Message"))))
				.modelPreferences(ModelPreferences.builder()
						.hints(List.of(ModelHint.of("OpenAi"), ModelHint.of("Ollama")))
						.costPriority(1.0)
						.speedPriority(1.0)
						.intelligencePriority(1.0)
						.build())
				.build();

		CreateMessageResult samplingResponse = exchange.createMessage(createMessageRequest);

		exchange.progressNotification(
				new ProgressNotification(progressToken, 1.0, 1.0, "sampling completed"));

		exchange.loggingNotification(LoggingMessageNotification.builder().data("Tool1 Done!").build());

		return "CALL RESPONSE: " + samplingResponse.toString() + ", " + elicitationResult.toString();
	}

}
