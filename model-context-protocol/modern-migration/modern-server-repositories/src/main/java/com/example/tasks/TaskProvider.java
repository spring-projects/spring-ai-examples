package com.example.tasks;

import org.springframework.ai.mcp.annotation.McpArg;
import org.springframework.ai.mcp.annotation.McpIcon;
import org.springframework.ai.mcp.annotation.McpPrompt;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

/**
 * The annotated tools and prompt of the other samples. The annotation scanner turns them into
 * the {@code List<McpSyncTool>} and {@code List<McpSyncPrompt>} beans that the repositories
 * are built from.
 * <p>
 * There are no {@code @McpResource} methods here: this server defines its own resource
 * repository ({@link TaskResourceRepository}), and with it the annotated resources would not
 * be served.
 * <p>
 * {@code add_task} and the prompt also carry definition attributes: an icon, which the
 * client can render next to the tool, and a display title for the prompt argument. They
 * are plain annotation attributes and work the same on every server kind.
 */
@Component
public class TaskProvider {

	private final TaskService tasks;

	public TaskProvider(TaskService tasks) {
		this.tasks = tasks;
	}

	@McpTool(name = "list_tasks", description = "Lists all tasks as JSON")
	public String listTasks() {
		return this.tasks.toJson(this.tasks.list());
	}

	@McpTool(name = "add_task", description = "Adds a task and returns its id",
			icons = @McpIcon(src = "https://example.com/icons/task-add.svg", mimeType = "image/svg+xml", sizes = "any"))
	public String addTask(@McpToolParam(description = "Title of the task") String title) {
		return this.tasks.add(title).id();
	}

	@McpTool(name = "complete_task", description = "Marks a task as done")
	public String completeTask(@McpToolParam(description = "Task id, e.g. t-1") String id) {
		return this.tasks.complete(id).map(task -> "Completed " + task.id()).orElse("No such task: " + id);
	}

	@McpPrompt(name = "daily-summary", description = "Asks the model to summarise the open tasks for an owner")
	public String dailySummary(
			@McpArg(name = "owner", title = "Owner", description = "Who the summary is for", required = true) String owner) {
		return "Summarise the open tasks for " + owner + " in two sentences: " + this.tasks.toJson(this.tasks.list());
	}

}
