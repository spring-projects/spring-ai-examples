package com.example.tickets;

import java.util.List;

import io.modelcontextprotocol.modern.McpException;
import io.modelcontextprotocol.modern.McpSchema.CacheScope;
import io.modelcontextprotocol.modern.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.modern.McpSchema.Resource;
import io.modelcontextprotocol.modern.McpSchema.ResourceTemplate;
import io.modelcontextprotocol.modern.McpSchema.TextResourceContents;
import io.modelcontextprotocol.modern.server.McpRequestContext;
import io.modelcontextprotocol.modern.server.McpSyncResponse;
import io.modelcontextprotocol.modern.server.feature.McpChangeBroadcaster;
import io.modelcontextprotocol.modern.server.feature.McpSyncResource;
import io.modelcontextprotocol.modern.server.feature.McpSyncResourceRepository;
import io.modelcontextprotocol.modern.server.feature.ResourceTemplatesPage;
import io.modelcontextprotocol.modern.server.feature.ResourcesPage;
import io.modelcontextprotocol.modern.server.feature.ServerChange;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Customises the resource repository by implementing the SDK interface. Every ticket is a
 * resource, {@code tickets://t-1}, {@code tickets://t-2} and so on, listed from {@link TicketService}
 * on each request: there is nothing to register, add or remove. The list is paged by ticket id.
 * <p>
 * Ticket changes arrive as Spring events and leave as MCP notifications on
 * {@code subscriptions/listen}: {@code resources/list_changed} when a ticket is opened or deleted,
 * {@code resources/updated} for the ticket's URI when it changes.
 */
@Component
public class TicketResourceRepository implements McpSyncResourceRepository {

	private static final String SCHEME = "tickets://";

	/** Small on purpose, so that the client sees a {@code nextCursor}. */
	private static final int PAGE_SIZE = 2;

	private final TicketService tickets;

	private final McpChangeBroadcaster changes;

	public TicketResourceRepository(TicketService tickets, McpChangeBroadcaster changes) {
		this.tickets = tickets;
		this.changes = changes;
	}

	@Override
	public ResourcesPage list(McpRequestContext ctx, String cursor) {
		// The cursor is the id of the last ticket on the previous page; ids are compared by
		// their number, so t-10 follows t-9.
		int afterNumber;
		try {
			afterNumber = cursor == null ? 0 : TicketService.number(cursor);
		}
		catch (IllegalArgumentException ex) {
			throw McpException.invalidParams("Unknown cursor: " + cursor);
		}
		List<TicketService.Ticket> after = this.tickets.list()
			.stream()
			.filter(ticket -> TicketService.number(ticket.id()) > afterNumber)
			.toList();
		List<Resource> page = after.stream().limit(PAGE_SIZE).map(TicketResourceRepository::resource).toList();
		String nextCursor = after.size() > PAGE_SIZE ? after.get(PAGE_SIZE - 1).id() : null;
		return new ResourcesPage(page, nextCursor, null, null);
	}

	@Override
	public ResourceTemplatesPage listTemplates(McpRequestContext ctx, String cursor) {
		return ResourceTemplatesPage.of(List.of(ResourceTemplate.builder(SCHEME + "{id}", "Ticket")
			.description("One ticket as JSON")
			.mimeType("application/json")
			.build()));
	}

	@Override
	public McpSyncResource find(McpRequestContext ctx, String uri) {
		if (!uri.startsWith(SCHEME) || this.tickets.find(uri.substring(SCHEME.length())).isEmpty()) {
			return null; // answered as an unknown resource
		}
		return McpSyncResource.of((readCtx, request) -> {
			String json = this.tickets.find(uri.substring(SCHEME.length()))
				.map(ticket -> this.tickets.toJson(List.of(ticket)))
				.orElse("null");
			return McpSyncResponse.result(
					ReadResourceResult.builder(List.of(TextResourceContents.builder(uri, json).mimeType("application/json").build()))
						.ttlMs(5_000)
						.cacheScope(CacheScope.PRIVATE)
						.build());
		});
	}

	@Override
	public boolean supportsSubscribe() {
		return true; // advertises resources.subscribe, so clients may listen to single URIs
	}

	@EventListener
	void onTicketChanged(TicketService.TicketChanged event) {
		if (event.kind() == TicketService.TicketChanged.Kind.UPDATED) {
			this.changes.broadcast(new ServerChange.ResourceUpdated(SCHEME + event.id()));
		}
		else {
			// Opened or deleted: the list changed; a subscriber of a deleted URI gets nothing it
			// could re-read.
			this.changes.broadcast(new ServerChange.ResourcesListChanged());
		}
	}

	private static Resource resource(TicketService.Ticket ticket) {
		return Resource.builder(SCHEME + ticket.id(), ticket.title())
			.description(ticket.resolved() ? "Resolved ticket" : "Open ticket")
			.mimeType("application/json")
			.build();
	}

}
