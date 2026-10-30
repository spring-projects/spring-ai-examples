package com.example.tasks;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.modelcontextprotocol.modern.McpSchema.CallToolOutcome;
import io.modelcontextprotocol.modern.McpSchema.CallToolResult;
import io.modelcontextprotocol.modern.McpSchema.CreateMessageRequest;
import io.modelcontextprotocol.modern.McpSchema.ElicitFormRequest;
import io.modelcontextprotocol.modern.McpSchema.InputRequiredResult;
import io.modelcontextprotocol.modern.McpSchema.Role;
import io.modelcontextprotocol.modern.McpSchema.SamplingMessage;
import io.modelcontextprotocol.modern.McpSchema.TextContent;
import io.modelcontextprotocol.modern.server.McpSyncNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.mcp.annotation.McpClientCapability;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.mcp.annotation.context.McpModernSyncRequestContext;
import org.springframework.stereotype.Component;

/**
 * The same two interactive tools as the STREAMABLE server, rewritten for the modern
 * protocol. There is no session, so the handler cannot block on the client: it returns
 * {@link InputRequiredResult} describing what it needs, and the client retries the same
 * call with {@code inputResponses} and the server-sealed {@code requestState}.
 * <p>
 * The response shape is the method's choice. {@code deleteTask} declares the SDK
 * {@link McpSyncNotifier}, so it answers with a short {@code text/event-stream} that
 * carries its progress notifications before the result. {@code summarizeTasks} declares
 * only the context and answers with plain JSON.
 * <p>
 * Neither tool can work without its client capability, so both declare it in
 * {@code requiredCapabilities}. A client whose request does not declare it gets JSON-RPC
 * error {@code -32021} before the method runs, and {@code deleteTask} opens no stream.
 */
@Component
public class InteractiveTaskProvider {

	private static final Logger logger = LoggerFactory.getLogger(InteractiveTaskProvider.class);

	/** Shape of an elicitation answer in {@code inputResponses}. */
	public record ElicitResponse(String action, Map<String, Object> content) {
	}

	private static final Map<String, Object> CONFIRM_SCHEMA = Map.of("type", "object", "properties",
			Map.of("confirmed", Map.of("type", "boolean")), "required", List.of("confirmed"));

	private final TaskService tasks;

	public InteractiveTaskProvider(TaskService tasks) {
		this.tasks = tasks;
	}

	@McpTool(name = "delete_task", description = "Deletes a task after asking the user to confirm",
			requiredCapabilities = McpClientCapability.ELICITATION_FORM)
	public CallToolOutcome deleteTask(McpSyncNotifier notifier, McpModernSyncRequestContext ctx,
			@McpToolParam(description = "Task id") String id) {
		logger.info("delete_task called for {}", id); // logging to the client is gone; log server-side
		Optional<ElicitResponse> answer = ctx.inputResponse("confirm", ElicitResponse.class);
		if (answer.isEmpty()) {
			// First round trip: ask. The task id travels in the sealed request state.
			notifier.progress(10, 100.0, "asking for confirmation");
			return InputRequiredResult.builder()
				.elicit("confirm", ElicitFormRequest.builder("Delete task " + id + "?", CONFIRM_SCHEMA).build())
				.requestState(id)
				.build();
		}
		// Second round trip: act on the answer.
		boolean confirmed = "accept".equals(answer.get().action()) && answer.get().content() != null
				&& Boolean.TRUE.equals(answer.get().content().get("confirmed"));
		if (!confirmed) {
			return text("Kept " + id);
		}
		notifier.progress(100, 100.0, "deleting");
		return text(this.tasks.delete(id) ? "Deleted " + id : "No such task: " + id);
	}

	@McpTool(name = "summarize_tasks", description = "Asks the client's model to summarise the task list",
			requiredCapabilities = McpClientCapability.SAMPLING)
	@SuppressWarnings("unchecked")
	public CallToolOutcome summarizeTasks(McpModernSyncRequestContext ctx) {
		Optional<Map> sampled = ctx.inputResponse("summary", Map.class);
		if (sampled.isEmpty()) {
			String prompt = "Summarise these tasks in one sentence: " + this.tasks.toJson(this.tasks.list());
			return InputRequiredResult.builder()
				.createMessage("summary", CreateMessageRequest
					.builder(List.of(SamplingMessage.builder(Role.USER, TextContent.builder(prompt).build()).build()), 300)
					.build())
				.build();
		}
		Object content = sampled.get().get("content");
		Object summary = content instanceof Map<?, ?> map ? map.get("text") : content;
		return text(String.valueOf(summary));
	}

	@McpTool(name = "echo", description = "Echoes the arguments")
	public String echo(Map<String, Object> arguments) {
		return String.valueOf(arguments);
	}

	private static CallToolResult text(String text) {
		return CallToolResult.builder().addContent(TextContent.builder(text).build()).build();
	}

}
