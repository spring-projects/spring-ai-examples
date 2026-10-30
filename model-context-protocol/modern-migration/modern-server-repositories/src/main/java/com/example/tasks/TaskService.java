package com.example.tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * The in-memory task store of the other samples, plus a {@link TaskChanged} event for every
 * change. The resource repository turns those events into MCP notifications, so the domain
 * code never talks to the MCP server.
 */
@Service
public class TaskService {

	public record Task(String id, String title, boolean done) {
	}

	/** Published after a task was added, completed or deleted. */
	public record TaskChanged(String id, Kind kind) {

		public enum Kind {

			ADDED, UPDATED, DELETED

		}

	}

	private final Map<String, Task> tasks = new ConcurrentHashMap<>();

	private final AtomicInteger ids = new AtomicInteger();

	private final ApplicationEventPublisher events;

	public TaskService(ApplicationEventPublisher events) {
		this.events = events;
		store(new Task("t-" + this.ids.incrementAndGet(), "Write the quarterly report", false));
		store(new Task("t-" + this.ids.incrementAndGet(), "Review the MCP migration guide", false));
		store(new Task("t-" + this.ids.incrementAndGet(), "Book the team offsite", true));
	}

	public Task add(String title) {
		Task task = store(new Task("t-" + this.ids.incrementAndGet(), title, false));
		this.events.publishEvent(new TaskChanged(task.id(), TaskChanged.Kind.ADDED));
		return task;
	}

	public List<Task> list() {
		List<Task> result = new ArrayList<>(this.tasks.values());
		result.sort((a, b) -> a.id().compareTo(b.id()));
		return Collections.unmodifiableList(result);
	}

	public Optional<Task> find(String id) {
		return Optional.ofNullable(this.tasks.get(id));
	}

	public Optional<Task> complete(String id) {
		return find(id).map(task -> {
			Task done = store(new Task(task.id(), task.title(), true));
			this.events.publishEvent(new TaskChanged(id, TaskChanged.Kind.UPDATED));
			return done;
		});
	}

	public boolean delete(String id) {
		boolean deleted = this.tasks.remove(id) != null;
		if (deleted) {
			this.events.publishEvent(new TaskChanged(id, TaskChanged.Kind.DELETED));
		}
		return deleted;
	}

	public String toJson(List<Task> tasks) {
		StringBuilder json = new StringBuilder("[");
		for (int i = 0; i < tasks.size(); i++) {
			Task task = tasks.get(i);
			if (i > 0) {
				json.append(',');
			}
			json.append("{\"id\":\"").append(task.id()).append("\",\"title\":\"")
				.append(task.title().replace("\"", "\\\"")).append("\",\"done\":").append(task.done()).append('}');
		}
		return json.append(']').toString();
	}

	private Task store(Task task) {
		this.tasks.put(task.id(), task);
		return task;
	}

}
