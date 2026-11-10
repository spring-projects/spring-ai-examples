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
import io.modelcontextprotocol.modern.server.McpAsyncNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import org.springframework.ai.mcp.annotation.McpClientCapability;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.mcp.annotation.context.McpModernRequestContext;
import org.springframework.stereotype.Component;

/**
 * The two multi round-trip tools of {@code modern-server-from-streamable}, written for
 * {@code type=ASYNC}. The flow is the same: the first call returns an
 * {@link InputRequiredResult}, and the client retries with {@code inputResponses} and the
 * sealed {@code requestState}.
 * <p>
 * {@code deleteTicket} declares the SDK {@link McpAsyncNotifier}, so it answers with a
 * short {@code text/event-stream}; {@code progress(...)} returns a {@code Mono<Void>} that
 * the method composes into its result. {@code summarizeTickets} declares only the
 * context, {@link McpModernRequestContext}, which is the same for both API types, and
 * answers with plain JSON.
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
	public Mono<CallToolOutcome> deleteTicket(McpAsyncNotifier notifier, McpModernRequestContext ctx,
			@McpToolParam(description = "Ticket id") String id) {
		logger.info("delete_ticket called for {}", id);
		ElicitResponse answer = ctx.inputResponse("confirm", ElicitResponse.class);
		if (answer == null) {
			// First round trip: ask. The ticket id travels in the sealed request state.
			return notifier.progress(10, 100.0, "asking for confirmation")
				.then(Mono.fromSupplier(() -> InputRequiredResult.builder()
					.elicit("confirm", ElicitFormRequest.builder("Delete ticket " + id + "?", CONFIRM_SCHEMA).build())
					.requestState(id)
					.build()));
		}
		// Second round trip: act on the ticket that was sealed into the request state; the id
		// argument of the retry is not trusted.
		String sealedId = ctx.requestState();
		boolean confirmed = "accept".equals(answer.action()) && answer.content() != null
				&& Boolean.TRUE.equals(answer.content().get("confirmed"));
		if (!confirmed) {
			return Mono.just(text("Kept " + sealedId));
		}
		return notifier.progress(100, 100.0, "deleting")
			.then(Mono.fromSupplier(() -> text(this.tickets.delete(sealedId) ? "Deleted " + sealedId : "No such ticket: " + sealedId)));
	}

	@McpTool(name = "summarize_tickets", description = "Asks the client's model to summarise the ticket list",
			requiredCapabilities = McpClientCapability.SAMPLING)
	public Mono<CallToolOutcome> summarizeTickets(McpModernRequestContext ctx) {
		Map<?, ?> sampled = ctx.inputResponse("summary", Map.class);
		if (sampled == null) {
			String prompt = "Summarise these tickets in one sentence: " + this.tickets.toJson(this.tickets.list());
			return Mono.just(InputRequiredResult.builder()
				.createMessage("summary", CreateMessageRequest
					.builder(List.of(SamplingMessage.builder(Role.USER, TextContent.builder(prompt).build()).build()), 300)
					.build())
				.build());
		}
		Object content = sampled.get("content");
		Object summary = content instanceof Map<?, ?> map ? map.get("text") : content;
		return Mono.just(text(String.valueOf(summary)));
	}

	private static CallToolOutcome text(String text) {
		return CallToolResult.builder().addContent(TextContent.builder(text).build()).build();
	}

}
