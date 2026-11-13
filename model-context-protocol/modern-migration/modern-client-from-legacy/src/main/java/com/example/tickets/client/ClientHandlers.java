package com.example.tickets.client;

import java.util.List;

import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.ElicitResult;
import io.modelcontextprotocol.spec.McpSchema.ProgressNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.mcp.annotation.McpElicitation;
import org.springframework.ai.mcp.annotation.McpProgress;
import org.springframework.ai.mcp.annotation.McpToolListChanged;
import org.springframework.ai.mcp.annotation.context.StructuredElicitResult;
import org.springframework.stereotype.Component;

/**
 * {@code ClientHandlers} of the legacy client after migration. The methods keep their
 * annotations and their legacy schema types: one method serves the legacy and the modern
 * connections that it names. Only the connection names changed, and two methods are gone:
 * {@code @McpSampling} and {@code @McpLogging}, as the modern protocol has neither sampling
 * nor client logging.
 * <p>
 * The {@code legacy} connection exists only with the {@code mixed} profile.
 */
@Component
public class ClientHandlers {

	private static final Logger logger = LoggerFactory.getLogger(ClientHandlers.class);

	public record Confirmation(boolean confirmed) {
	}

	/**
	 * Answers the form elicitation of both protocols. On a modern connection the SDK client
	 * calls it for the input request of the server and then retries the tool call. The
	 * {@link ToolContext} parameter is new and optional: it is the context of the tool call
	 * that the elicitation belongs to, so the method knows whom to ask.
	 */
	@McpElicitation(clients = { "tickets", "legacy" })
	public StructuredElicitResult<Confirmation> confirm(McpSchema.ElicitRequest request, ToolContext toolContext) {
		Object user = toolContext != null ? toolContext.getContext().get("user") : null;
		logger.info("ELICITATION{}: {}", user != null ? " for " + user : "", request.message());
		return new StructuredElicitResult<>(ElicitResult.Action.ACCEPT, new Confirmation(true), null);
	}

	@McpProgress(clients = { "tickets", "legacy" })
	public void progress(ProgressNotification notification) {
		logger.info("PROGRESS {}: {} {}", notification.progressToken(), notification.progress(), notification.message());
	}

	/**
	 * Called for a modern connection that sets {@code listen=true}: when its server reports
	 * that the tools changed, and when the listen stream is established again after it had
	 * ended. A modern server sends no list with the change, so the tools are listed first,
	 * for the application and not for a caller.
	 */
	@McpToolListChanged(clients = { "tickets", "legacy" })
	public void toolsChanged(List<McpSchema.Tool> tools) {
		logger.info("TOOLS LIST CHANGED: {}", tools.stream().map(McpSchema.Tool::name).toList());
	}

}
