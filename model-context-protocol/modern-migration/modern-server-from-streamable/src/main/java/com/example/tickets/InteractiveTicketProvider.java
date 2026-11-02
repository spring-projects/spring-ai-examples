package com.example.tickets;

import java.util.List;
import java.util.Map;

import io.modelcontextprotocol.modern.McpSchema.CallToolOutcome;
import io.modelcontextprotocol.modern.McpSchema.CallToolResult;
import io.modelcontextprotocol.modern.McpSchema.CreateMessageRequest;
import io.modelcontextprotocol.modern.McpSchema.ElicitFormRequest;
import io.modelcontextprotocol.modern.McpSchema.InputRequiredResult;
import io.modelcontextprotocol.modern.McpSchema.Role;
import io.modelcontextprotocol.modern.McpSchema.SamplingMessage;
import io.modelcontextprotocol.modern.McpSchema.TextContent;
import io.modelcontextprotocol.modern.server.McpSyncNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.mcp.annotation.McpClientCapability;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.mcp.annotation.context.McpModernSyncRequestContext;
import org.springframework.stereotype.Component;

/**
 * The same two interactive tools as the STREAMABLE server, rewritten for the modern
 * protocol. There is no session, so the handler cannot block on the client: it returns
 * {@link InputRequiredResult} describing what it needs, and the client retries the same
 * call with {@code inputResponses} and the server-sealed {@code requestState}.
 * <p>
 * The response shape is the method's choice. {@code deleteTicket} declares the SDK
 * {@link McpSyncNotifier}, so it answers with a short {@code text/event-stream} that
 * carries its progress notifications before the result. {@code summarizeTickets} declares
 * only the context and answers with plain JSON.
 * <p>
 * Neither tool can work without its client capability, so both declare it in
 * {@code requiredCapabilities}. A client whose request does not declare it gets JSON-RPC
 * error {@code -32021} before the method runs, and {@code deleteTicket} opens no stream.
 */
@Component
public class InteractiveTicketProvider {

	private static final Logger logger = LoggerFactory.getLogger(InteractiveTicketProvider.class);

	/** Shape of an elicitation answer in {@code inputResponses}. */
	public record ElicitResponse(String action, Map<String, Object> content) {
	}

	private static final Map<String, Object> CONFIRM_SCHEMA = Map.of("type", "object", "properties",
			Map.of("confirmed", Map.of("type", "boolean")), "required", List.of("confirmed"));

	private final TicketService tickets;

	public InteractiveTicketProvider(TicketService tickets) {
		this.tickets = tickets;
	}

	@McpTool(name = "delete_ticket", description = "Deletes a ticket after asking the user to confirm",
			requiredCapabilities = McpClientCapability.ELICITATION_FORM)
	public CallToolOutcome deleteTicket(McpSyncNotifier notifier, McpModernSyncRequestContext ctx,
			@McpToolParam(description = "Ticket id") String id) {
		logger.info("delete_ticket called for {}", id); // logging to the client is gone; log server-side
		ElicitResponse answer = ctx.inputResponse("confirm", ElicitResponse.class);
		if (answer == null) {
			// First round trip: ask. The ticket id travels in the sealed request state.
			notifier.progress(10, 100.0, "asking for confirmation");
			return InputRequiredResult.builder()
				.elicit("confirm", ElicitFormRequest.builder("Delete ticket " + id + "?", CONFIRM_SCHEMA).build())
				.requestState(id)
				.build();
		}
		// Second round trip: act on the answer, for the ticket that was sealed into the request
		// state on the first round. The seal covers the tool and the caller, not the arguments,
		// so the id argument of the retry is not trusted.
		String sealedId = ctx.requestState();
		boolean confirmed = "accept".equals(answer.action()) && answer.content() != null
				&& Boolean.TRUE.equals(answer.content().get("confirmed"));
		if (!confirmed) {
			return text("Kept " + sealedId);
		}
		notifier.progress(100, 100.0, "deleting");
		return text(this.tickets.delete(sealedId) ? "Deleted " + sealedId : "No such ticket: " + sealedId);
	}

	@McpTool(name = "summarize_tickets", description = "Asks the client's model to summarise the ticket list",
			requiredCapabilities = McpClientCapability.SAMPLING)
	public CallToolOutcome summarizeTickets(McpModernSyncRequestContext ctx) {
		Map<?, ?> sampled = ctx.inputResponse("summary", Map.class);
		if (sampled == null) {
			String prompt = "Summarise these tickets in one sentence: " + this.tickets.toJson(this.tickets.list());
			return InputRequiredResult.builder()
				.createMessage("summary", CreateMessageRequest
					.builder(List.of(SamplingMessage.builder(Role.USER, TextContent.builder(prompt).build()).build()), 300)
					.build())
				.build();
		}
		Object content = sampled.get("content");
		Object summary = content instanceof Map<?, ?> map ? map.get("text") : content;
		return text(String.valueOf(summary));
	}

	private static CallToolResult text(String text) {
		return CallToolResult.builder().addContent(TextContent.builder(text).build()).build();
	}

}
