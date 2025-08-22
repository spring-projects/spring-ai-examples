package org.springframework.ai.mcp.samples.client;

import java.util.Map;

import org.springaicommunity.mcp.annotation.McpElicitation;
import org.springaicommunity.mcp.annotation.McpLogging;
import org.springaicommunity.mcp.annotation.McpProgress;
import org.springaicommunity.mcp.annotation.McpSampling;
import org.springframework.stereotype.Service;

import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;
import io.modelcontextprotocol.spec.McpSchema.ElicitRequest;
import io.modelcontextprotocol.spec.McpSchema.ElicitResult;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;

@Service
public class ClientMcpHandlers {

	@McpProgress(clientId = "server1")
	public void progress(McpSchema.ProgressNotification progressNotification) {
		System.out.println("MCP PROGRESS: [" + progressNotification.progressToken() + "] progress: "
				+ progressNotification.progress() + " total: " + progressNotification.total() + " message: "
				+ progressNotification.message());
	}

	@McpLogging
	public void logging(LoggingMessageNotification loggingMessage) {
		System.out.println("MCP LOGGING: [" + loggingMessage.level() + "] " + loggingMessage.data());
	}

	@McpSampling
	public CreateMessageResult sampling(McpSchema.CreateMessageRequest llmRequest) {
		String userPrompt = ((McpSchema.TextContent) llmRequest.messages().get(0).content()).text();
		String modelHint = llmRequest.modelPreferences().hints().get(0).name();
		return CreateMessageResult.builder()
				.content(new McpSchema.TextContent("Response " + userPrompt + " with model hint " + modelHint))
				.build();
	};

	@McpElicitation
	public ElicitResult elicit(ElicitRequest request) {
		return new ElicitResult(ElicitResult.Action.ACCEPT, Map.of("message", request.message()));
	}

}
