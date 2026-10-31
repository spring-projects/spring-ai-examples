package com.example.tickets;

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
 * The in-memory ticket store of the other samples, plus a {@link TicketChanged} event for every
 * change. The resource repository turns those events into MCP notifications, so the domain
 * code never talks to the MCP server.
 */
@Service
public class TicketService {

	public record Ticket(String id, String title, boolean resolved) {
	}

	/** Published after a ticket was opened, resolved or deleted. */
	public record TicketChanged(String id, Kind kind) {

		public enum Kind {

			ADDED, UPDATED, DELETED

		}

	}

	private final Map<String, Ticket> tickets = new ConcurrentHashMap<>();

	private final AtomicInteger ids = new AtomicInteger();

	private final ApplicationEventPublisher events;

	public TicketService(ApplicationEventPublisher events) {
		this.events = events;
		store(new Ticket("t-" + this.ids.incrementAndGet(), "Write the quarterly report", false));
		store(new Ticket("t-" + this.ids.incrementAndGet(), "Review the MCP migration guide", false));
		store(new Ticket("t-" + this.ids.incrementAndGet(), "Book the team offsite", true));
	}

	public Ticket open(String title) {
		Ticket ticket = store(new Ticket("t-" + this.ids.incrementAndGet(), title, false));
		this.events.publishEvent(new TicketChanged(ticket.id(), TicketChanged.Kind.ADDED));
		return ticket;
	}

	public List<Ticket> list() {
		List<Ticket> result = new ArrayList<>(this.tickets.values());
		result.sort((a, b) -> a.id().compareTo(b.id()));
		return Collections.unmodifiableList(result);
	}

	public Optional<Ticket> find(String id) {
		return Optional.ofNullable(this.tickets.get(id));
	}

	public Optional<Ticket> resolve(String id) {
		return find(id).map(ticket -> {
			Ticket resolved = store(new Ticket(ticket.id(), ticket.title(), true));
			this.events.publishEvent(new TicketChanged(id, TicketChanged.Kind.UPDATED));
			return resolved;
		});
	}

	public boolean delete(String id) {
		boolean deleted = this.tickets.remove(id) != null;
		if (deleted) {
			this.events.publishEvent(new TicketChanged(id, TicketChanged.Kind.DELETED));
		}
		return deleted;
	}

	public String toJson(List<Ticket> tickets) {
		StringBuilder json = new StringBuilder("[");
		for (int i = 0; i < tickets.size(); i++) {
			Ticket ticket = tickets.get(i);
			if (i > 0) {
				json.append(',');
			}
			json.append("{\"id\":\"").append(ticket.id()).append("\",\"title\":\"")
				.append(ticket.title().replace("\"", "\\\"")).append("\",\"resolved\":").append(ticket.resolved()).append('}');
		}
		return json.append(']').toString();
	}

	private Ticket store(Ticket ticket) {
		this.tickets.put(ticket.id(), ticket);
		return ticket;
	}

}
