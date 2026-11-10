package com.example.tickets;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import io.modelcontextprotocol.modern.McpSchema.CacheScope;
import reactor.core.publisher.Mono;

import org.springframework.ai.mcp.annotation.McpArg;
import org.springframework.ai.mcp.annotation.McpComplete;
import org.springframework.ai.mcp.annotation.McpPrompt;
import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

/**
 * The tools, resources and prompt of the other servers, written for
 * {@code spring.ai.mcp.server.type=ASYNC}. An MCP request has one result, so a method
 * returns a single value: a {@code Mono}, a {@code CompletableFuture}, or a plain value.
 * A method that returns a {@code Flux} is rejected at startup; return a {@code Mono} of a
 * list instead.
 */
@Component
public class TicketProvider {

	/** The people the daily summary is written for; tickets have no owner of their own. */
	private static final List<String> OWNERS = List.of("Ana", "Andrei", "Ben", "Chloe");

	private final TicketService tickets;

	public TicketProvider(TicketService tickets) {
		this.tickets = tickets;
	}

	@McpTool(name = "list_tickets", description = "Lists all tickets as JSON")
	public Mono<String> listTickets() {
		return Mono.fromSupplier(() -> this.tickets.toJson(this.tickets.list()));
	}

	@McpTool(name = "open_ticket", description = "Opens a ticket and returns its id")
	public Mono<String> openTicket(@McpToolParam(description = "Title of the ticket") String title) {
		return Mono.fromSupplier(() -> this.tickets.open(title).id());
	}

	// A plain return type works too: the server runs the method on Reactor's boundedElastic
	// scheduler, so it may block without stalling a WebFlux event loop.
	@McpTool(name = "resolve_ticket", description = "Marks a ticket as resolved")
	public String resolveTicket(@McpToolParam(description = "Ticket id, e.g. t-1") String id) {
		return this.tickets.resolve(id).map(ticket -> "Resolved " + ticket.id()).orElse("No such ticket: " + id);
	}

	// Any single-valued asynchronous type is resolved through Spring's ReactiveAdapterRegistry.
	@McpTool(name = "count_open_tickets", description = "Counts the tickets that are not resolved")
	public CompletableFuture<String> countOpenTickets() {
		return CompletableFuture
			.supplyAsync(() -> String.valueOf(this.tickets.list().stream().filter(ticket -> !ticket.resolved()).count()));
	}

	@McpResource(uri = "tickets://all", name = "All tickets", description = "Every ticket as a JSON array",
			mimeType = "application/json", ttlMs = 30_000, cacheScope = CacheScope.PUBLIC)
	public Mono<String> allTickets() {
		return Mono.fromSupplier(() -> this.tickets.toJson(this.tickets.list()));
	}

	@McpResource(uri = "tickets://{id}", name = "Ticket", description = "One ticket as JSON", mimeType = "application/json")
	public Mono<String> oneTicket(String id) {
		return Mono.fromSupplier(() -> this.tickets.find(id).map(ticket -> this.tickets.toJson(List.of(ticket))).orElse("null"));
	}

	// Completes the {id} variable of tickets://{id}. The handler travels with that template, so
	// it must name an @McpResource template of this application.
	@McpComplete(uri = "tickets://{id}")
	public Mono<List<String>> completeTicketId(String prefix) {
		return Mono.fromSupplier(() -> this.tickets.list().stream().map(ticket -> ticket.id()).filter(id -> id.startsWith(prefix)).toList());
	}

	@McpPrompt(name = "daily-summary", description = "Asks the model to summarise the open tickets for an owner")
	public Mono<String> dailySummary(@McpArg(name = "owner", description = "Who the summary is for", required = true) String owner) {
		return Mono.fromSupplier(() -> "Summarise the open tickets for " + owner + " in two sentences: " + this.tickets.toJson(this.tickets.list()));
	}

	// Completes the owner argument of daily-summary, which the server attaches to that prompt.
	@McpComplete(prompt = "daily-summary")
	public Mono<List<String>> completeOwner(String prefix) {
		return Mono.just(OWNERS.stream().filter(owner -> owner.toLowerCase().startsWith(prefix.toLowerCase())).toList());
	}

}
