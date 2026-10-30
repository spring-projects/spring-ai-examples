package com.example.tasks;

import java.util.Map;

import io.modelcontextprotocol.spec.McpSchema.ElicitResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.ai.mcp.annotation.context.StructuredElicitResult;
import org.springframework.stereotype.Component;

/**
 * Tools that talk back to the client over the session: logging, progress, elicitation and
 * sampling. These are the methods a migration to the modern server has to rewrite,
 * because the modern protocol has no session to call back on.
 */
@Component
public class InteractiveTaskProvider {

	public record Confirmation(boolean confirmed) {
	}

	private final TaskService tasks;

	public InteractiveTaskProvider(TaskService tasks) {
		this.tasks = tasks;
	}

	@McpTool(name = "delete_task", description = "Deletes a task after asking the user to confirm")
	public String deleteTask(McpSyncRequestContext ctx, @McpToolParam(description = "Task id") String id) {
		ctx.info("delete_task called for " + id); // log message sent to the client
		ctx.progress(p -> p.percentage(10).message("asking for confirmation"));

		// Blocking round trip: the server asks, the client answers, the handler continues.
		StructuredElicitResult<Confirmation> answer = ctx.elicit(e -> e.message("Delete task " + id + "?"),
				Confirmation.class);
		if (answer.action() != ElicitResult.Action.ACCEPT || answer.structuredContent() == null
				|| !answer.structuredContent().confirmed()) {
			return "Kept " + id;
		}
		ctx.progress(p -> p.percentage(100).message("deleting"));
		return this.tasks.delete(id) ? "Deleted " + id : "No such task: " + id;
	}

	@McpTool(name = "summarize_tasks", description = "Asks the client's model to summarise the task list")
	public String summarizeTasks(McpSyncRequestContext ctx) {
		var result = ctx.sample(s -> s.message("Summarise these tasks in one sentence: " + this.tasks.toJson(this.tasks.list())));
		return result.content() instanceof TextContent text ? text.text() : String.valueOf(result.content());
	}

	@McpTool(name = "echo", description = "Echoes the arguments")
	public String echo(Map<String, Object> arguments) {
		return String.valueOf(arguments);
	}

}
