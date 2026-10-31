package com.example.tickets;

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
 * repository ({@link TicketResourceRepository}), and with it the annotated resources would not
 * be served.
 * <p>
 * {@code open_ticket} and the prompt also carry definition attributes: an icon, which the
 * client can render next to the tool, and a display title for the prompt argument. They
 * are plain annotation attributes and work the same on every server kind.
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

	@McpTool(name = "open_ticket", description = "Opens a ticket and returns its id",
			icons = @McpIcon(src = "https://example.com/icons/ticket-add.svg", mimeType = "image/svg+xml", sizes = "any"))
	public String openTicket(@McpToolParam(description = "Title of the ticket") String title) {
		return this.tickets.open(title).id();
	}

	@McpTool(name = "resolve_ticket", description = "Marks a ticket as resolved")
	public String resolveTicket(@McpToolParam(description = "Ticket id, e.g. t-1") String id) {
		return this.tickets.resolve(id).map(ticket -> "Resolved " + ticket.id()).orElse("No such ticket: " + id);
	}

	@McpPrompt(name = "daily-summary", description = "Asks the model to summarise the open tickets for an owner")
	public String dailySummary(
			@McpArg(name = "owner", title = "Owner", description = "Who the summary is for", required = true) String owner) {
		return "Summarise the open tickets for " + owner + " in two sentences: " + this.tickets.toJson(this.tickets.list());
	}

}
