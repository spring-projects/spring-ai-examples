package com.example.tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

/**
 * In-memory task store shared by all the sample servers. It is deliberately tiny: the
 * samples are about the MCP protocol, not about tasks.
 */
@Service
public class TaskService {

	public record Task(String id, String title, boolean done) {
	}

	private final Map<String, Task> tasks = new ConcurrentHashMap<>();

	private final AtomicInteger ids = new AtomicInteger();

	public TaskService() {
		add("Write the quarterly report");
		add("Review the MCP migration guide");
	}

	public Task add(String title) {
		Task task = new Task("t-" + ids.incrementAndGet(), title, false);
		this.tasks.put(task.id(), task);
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
			Task done = new Task(task.id(), task.title(), true);
			this.tasks.put(id, done);
			return done;
		});
	}

	public boolean delete(String id) {
		return this.tasks.remove(id) != null;
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

}
