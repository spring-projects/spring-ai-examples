package com.example.tasks;

import java.util.List;

import io.modelcontextprotocol.modern.McpSchema.CacheScope;
import io.modelcontextprotocol.modern.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.modern.McpSchema.Resource;
import io.modelcontextprotocol.modern.McpSchema.ResourceTemplate;
import io.modelcontextprotocol.modern.McpSchema.TextResourceContents;
import io.modelcontextprotocol.modern.server.McpRequestContext;
import io.modelcontextprotocol.modern.server.McpSyncResponse;
import io.modelcontextprotocol.modern.server.feature.McpChangeBroadcaster;
import io.modelcontextprotocol.modern.server.feature.McpSyncResource;
import io.modelcontextprotocol.modern.server.feature.McpSyncResourceRepository;
import io.modelcontextprotocol.modern.server.feature.ResourceTemplatesPage;
import io.modelcontextprotocol.modern.server.feature.ResourcesPage;
import io.modelcontextprotocol.modern.server.feature.ServerChange;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Customises the resource repository by implementing the SDK interface. Every task is a
 * resource, {@code tasks://t-1}, {@code tasks://t-2} and so on, listed from {@link TaskService}
 * on each request: there is nothing to register, add or remove. The list is paged by task id.
 * <p>
 * Task changes arrive as Spring events and leave as MCP notifications on
 * {@code subscriptions/listen}: {@code resources/list_changed} when a task is added or deleted,
 * {@code resources/updated} for the task's URI when it changes.
 */
@Component
public class TaskResourceRepository implements McpSyncResourceRepository {

	private static final String SCHEME = "tasks://";

	/** Small on purpose, so that the client sees a {@code nextCursor}. */
	private static final int PAGE_SIZE = 2;

	private final TaskService tasks;

	private final McpChangeBroadcaster changes;

	public TaskResourceRepository(TaskService tasks, McpChangeBroadcaster changes) {
		this.tasks = tasks;
		this.changes = changes;
	}

	@Override
	public ResourcesPage list(McpRequestContext ctx, String cursor) {
		// The cursor is the id of the last task on the previous page.
		List<TaskService.Task> after = this.tasks.list()
			.stream()
			.filter(task -> cursor == null || task.id().compareTo(cursor) > 0)
			.toList();
		List<Resource> page = after.stream().limit(PAGE_SIZE).map(TaskResourceRepository::resource).toList();
		String nextCursor = after.size() > PAGE_SIZE ? after.get(PAGE_SIZE - 1).id() : null;
		return new ResourcesPage(page, nextCursor, null, null);
	}

	@Override
	public ResourceTemplatesPage listTemplates(McpRequestContext ctx, String cursor) {
		return ResourceTemplatesPage.of(List.of(ResourceTemplate.builder(SCHEME + "{id}", "Task")
			.description("One task as JSON")
			.mimeType("application/json")
			.build()));
	}

	@Override
	public McpSyncResource find(McpRequestContext ctx, String uri) {
		if (!uri.startsWith(SCHEME) || this.tasks.find(uri.substring(SCHEME.length())).isEmpty()) {
			return null; // answered as an unknown resource
		}
		return McpSyncResource.of((readCtx, request) -> {
			String json = this.tasks.find(uri.substring(SCHEME.length()))
				.map(task -> this.tasks.toJson(List.of(task)))
				.orElse("null");
			return McpSyncResponse.result(
					ReadResourceResult.builder(List.of(TextResourceContents.builder(uri, json).mimeType("application/json").build()))
						.ttlMs(5_000)
						.cacheScope(CacheScope.PRIVATE)
						.build());
		});
	}

	@Override
	public boolean supportsSubscribe() {
		return true; // advertises resources.subscribe, so clients may listen to single URIs
	}

	@EventListener
	void onTaskChanged(TaskService.TaskChanged event) {
		if (event.kind() != TaskService.TaskChanged.Kind.UPDATED) {
			this.changes.broadcast(new ServerChange.ResourcesListChanged());
		}
		this.changes.broadcast(new ServerChange.ResourceUpdated(SCHEME + event.id()));
	}

	private static Resource resource(TaskService.Task task) {
		return Resource.builder(SCHEME + task.id(), task.title())
			.description(task.done() ? "Completed task" : "Open task")
			.mimeType("application/json")
			.build();
	}

}
