package com.example.tickets;


import io.modelcontextprotocol.spec.McpSchema.ElicitResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.ai.mcp.annotation.context.StructuredElicitResult;
import org.springframework.stereotype.Component;

/**
 * Tools that talk back to the client over the session: logging, progress, elicitation and
 * sampling. These are the methods a migration to the modern server has to rewrite,
 * because the modern protocol has no session to call back on.
 */
@Component
public class InteractiveTicketProvider {

	public record Confirmation(boolean confirmed) {
	}

	private final TicketService tickets;

	public InteractiveTicketProvider(TicketService tickets) {
		this.tickets = tickets;
	}

	@McpTool(name = "delete_ticket", description = "Deletes a ticket after asking the user to confirm")
	public String deleteTicket(McpSyncRequestContext ctx, @McpToolParam(description = "Ticket id") String id) {
		ctx.info("delete_ticket called for " + id); // log message sent to the client
		ctx.progress(p -> p.percentage(10).message("asking for confirmation"));

		// Blocking round trip: the server asks, the client answers, the handler continues.
		StructuredElicitResult<Confirmation> answer = ctx.elicit(e -> e.message("Delete ticket " + id + "?"),
				Confirmation.class);
		if (answer.action() != ElicitResult.Action.ACCEPT || answer.structuredContent() == null
				|| !answer.structuredContent().confirmed()) {
			return "Kept " + id;
		}
		ctx.progress(p -> p.percentage(100).message("deleting"));
		return this.tickets.delete(id) ? "Deleted " + id : "No such ticket: " + id;
	}

	@McpTool(name = "summarize_tickets", description = "Asks the client's model to summarise the ticket list")
	public String summarizeTickets(McpSyncRequestContext ctx) {
		var result = ctx.sample(s -> s.message("Summarise these tickets in one sentence: " + this.tickets.toJson(this.tickets.list())));
		return result.content() instanceof TextContent text ? text.text() : String.valueOf(result.content());
	}

}
