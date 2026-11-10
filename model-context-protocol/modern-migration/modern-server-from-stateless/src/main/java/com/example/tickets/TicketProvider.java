package com.example.tickets;

import java.util.List;

import io.modelcontextprotocol.modern.McpSchema.CacheScope;

import org.springframework.ai.mcp.annotation.McpArg;
import org.springframework.ai.mcp.annotation.McpComplete;
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
public class TicketProvider {

	private final TicketService tickets;

	public TicketProvider(TicketService tickets) {
		this.tickets = tickets;
	}

	@McpTool(name = "list_tickets", description = "Lists all tickets as JSON")
	public String listTickets() {
		return this.tickets.toJson(this.tickets.list());
	}

	@McpTool(name = "open_ticket", description = "Opens a ticket and returns its id")
	public String openTicket(@McpToolParam(description = "Title of the ticket") String title) {
		return this.tickets.open(title).id();
	}

	@McpTool(name = "resolve_ticket", description = "Marks a ticket as resolved")
	public String resolveTicket(@McpToolParam(description = "Ticket id, e.g. t-1") String id) {
		return this.tickets.resolve(id).map(ticket -> "Resolved " + ticket.id()).orElse("No such ticket: " + id);
	}

	@McpResource(uri = "tickets://all", name = "All tickets", description = "Every ticket as a JSON array",
			mimeType = "application/json",
			ttlMs = 30_000, cacheScope = CacheScope.PUBLIC)
	public String allTickets() {
		return this.tickets.toJson(this.tickets.list());
	}

	@McpResource(uri = "tickets://{id}", name = "Ticket", description = "One ticket as JSON", mimeType = "application/json")
	public String oneTicket(String id) {
		return this.tickets.find(id).map(ticket -> this.tickets.toJson(List.of(ticket))).orElse("null");
	}

	// Completes the {id} variable of tickets://{id}. On the modern server the handler travels
	// with that template, so it must name an @McpResource template of this application.
	@McpComplete(uri = "tickets://{id}")
	public List<String> completeTicketId(String prefix) {
		return this.tickets.list().stream().map(ticket -> ticket.id()).filter(id -> id.startsWith(prefix)).toList();
	}

	@McpPrompt(name = "daily-summary", description = "Asks the model to summarise the open tickets for an owner")
	public String dailySummary(@McpArg(name = "owner", description = "Who the summary is for", required = true) String owner) {
		return "Summarise the open tickets for " + owner + " in two sentences: " + this.tickets.toJson(this.tickets.list());
	}

}
