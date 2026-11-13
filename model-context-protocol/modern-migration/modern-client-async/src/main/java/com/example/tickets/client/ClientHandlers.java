package com.example.tickets.client;

import java.util.List;

import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.ElicitResult;
import io.modelcontextprotocol.spec.McpSchema.ProgressNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.mcp.annotation.McpElicitation;
import org.springframework.ai.mcp.annotation.McpProgress;
import org.springframework.ai.mcp.annotation.McpToolListChanged;
import org.springframework.ai.mcp.annotation.context.StructuredElicitResult;
import org.springframework.stereotype.Component;

/**
 * The handlers of the legacy client, written for the asynchronous client type: the same
 * annotations and the same legacy schema types, but each method returns a {@code Mono}. One
 * method serves the legacy and the modern connections that it names.
 * <p>
 * With {@code spring.ai.mcp.client.type=ASYNC} only reactive methods are called. A
 * synchronous method that names a connection is reported with a warning at startup.
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
	 * optional {@link ToolContext} is the context of the tool call that the elicitation
	 * belongs to, so the method knows whom to ask. An application would ask the user here
	 * and complete the {@code Mono} with the answer.
	 */
	@McpElicitation(clients = { "tickets", "legacy" })
	public Mono<StructuredElicitResult<Confirmation>> confirm(McpSchema.ElicitRequest request,
			ToolContext toolContext) {
		Object user = toolContext != null ? toolContext.getContext().get("user") : null;
		logger.info("ELICITATION{}: {}", user != null ? " for " + user : "", request.message());
		return Mono.just(new StructuredElicitResult<>(ElicitResult.Action.ACCEPT, new Confirmation(true), null));
	}

	/** A progress method of the asynchronous type returns {@code Mono<Void>}. */
	@McpProgress(clients = { "tickets", "legacy" })
	public Mono<Void> progress(ProgressNotification notification) {
		return Mono.fromRunnable(() -> logger.info("PROGRESS {}: {} {}", notification.progressToken(),
				notification.progress(), notification.message()));
	}

	/**
	 * Called for a modern connection that sets {@code listen=true}: when its server reports
	 * that the tools changed, and when the listen stream is established again after it had
	 * ended. A modern server sends no list with the change, so the tools are listed first,
	 * for the application and not for a caller.
	 */
	@McpToolListChanged(clients = { "tickets", "legacy" })
	public Mono<Void> toolsChanged(List<McpSchema.Tool> tools) {
		return Mono.fromRunnable(
				() -> logger.info("TOOLS LIST CHANGED: {}", tools.stream().map(McpSchema.Tool::name).toList()));
	}

}
