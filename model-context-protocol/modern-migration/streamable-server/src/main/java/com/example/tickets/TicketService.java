package com.example.tickets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

/**
 * In-memory ticket store shared by all the sample servers. It is deliberately tiny: the
 * samples are about the MCP protocol, not about tickets.
 */
@Service
public class TicketService {

	public record Ticket(String id, String title, boolean resolved) {
	}

	private static final JsonMapper JSON = JsonMapper.builder().build();

	private final Map<String, Ticket> tickets = new ConcurrentHashMap<>();

	private final AtomicInteger ids = new AtomicInteger();

	public TicketService() {
		open("Write the quarterly report");
		open("Review the MCP migration guide");
	}

	public Ticket open(String title) {
		Ticket ticket = new Ticket("t-" + ids.incrementAndGet(), title, false);
		this.tickets.put(ticket.id(), ticket);
		return ticket;
	}

	public List<Ticket> list() {
		List<Ticket> result = new ArrayList<>(this.tickets.values());
		result.sort(Comparator.comparingInt(ticket -> number(ticket.id())));
		return Collections.unmodifiableList(result);
	}

	public Optional<Ticket> find(String id) {
		return Optional.ofNullable(this.tickets.get(id));
	}

	public Optional<Ticket> resolve(String id) {
		return find(id).map(ticket -> {
			Ticket resolved = new Ticket(ticket.id(), ticket.title(), true);
			this.tickets.put(id, resolved);
			return resolved;
		});
	}

	public boolean delete(String id) {
		return this.tickets.remove(id) != null;
	}

	public String toJson(List<Ticket> tickets) {
		return JSON.writeValueAsString(tickets);
	}

	/** The number behind {@code t-}, which orders the ids and pages them. */
	static int number(String id) {
		if (!id.startsWith("t-")) {
			throw new IllegalArgumentException("Not a ticket id: " + id);
		}
		return Integer.parseInt(id.substring(2));
	}

}
