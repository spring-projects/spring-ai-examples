package com.example.tasks;

import java.util.List;

import io.modelcontextprotocol.modern.McpSchema.CacheScope;

import org.springframework.ai.mcp.annotation.McpArg;
import org.springframework.ai.mcp.annotation.McpPrompt;
import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

/**
 * Tools, resources and a prompt that need nothing from the session. These methods are
 * byte-for-byte the same on every protocol; only the annotation attributes marked below
 * differ on the modern server.
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

	@McpTool(name = "add_task", description = "Adds a task and returns its id")
	public String addTask(@McpToolParam(description = "Title of the task") String title) {
		return this.tasks.add(title).id();
	}

	@McpTool(name = "complete_task", description = "Marks a task as done")
	public String completeTask(@McpToolParam(description = "Task id, e.g. t-1") String id) {
		return this.tasks.complete(id).map(task -> "Completed " + task.id()).orElse("No such task: " + id);
	}

	@McpResource(uri = "tasks://all", name = "All tasks", description = "Every task as a JSON array",
			mimeType = "application/json",
			ttlMs = 30_000, cacheScope = CacheScope.PUBLIC)
	public String allTasks() {
		return this.tasks.toJson(this.tasks.list());
	}

	@McpResource(uri = "tasks://{id}", name = "Task", description = "One task as JSON", mimeType = "application/json")
	public String oneTask(String id) {
		return this.tasks.find(id).map(task -> this.tasks.toJson(List.of(task))).orElse("null");
	}

	@McpPrompt(name = "daily-summary", description = "Asks the model to summarise the open tasks for an owner")
	public String dailySummary(@McpArg(name = "owner", description = "Who the summary is for", required = true) String owner) {
		return "Summarise the open tasks for " + owner + " in two sentences: " + this.tasks.toJson(this.tasks.list());
	}

}
