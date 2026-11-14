package com.example.tickets.client;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.modelcontextprotocol.modern.client.SubscriptionEvent;
import reactor.core.publisher.Mono;

import org.springframework.ai.mcp.modern.client.AsyncModernMcpChangeListener;
import org.springframework.ai.mcp.modern.client.McpModernConnectionInfo;

/**
 * Prints what the listen streams of the modern connections report. An
 * {@link AsyncModernMcpChangeListener} bean hears it next to the tool callback provider,
 * which drops a kept tool listing, and the {@code @McpToolListChanged} methods. Its methods
 * return a {@code Mono}, and the next listener is told when it completes, so a listener must
 * not block.
 */
class ListenLog implements AsyncModernMcpChangeListener {

	private final CountDownLatch established = new CountDownLatch(1);

	@Override
	public Mono<Void> changed(McpModernConnectionInfo connection, SubscriptionEvent event) {
		return Mono.fromRunnable(() -> System.out
			.println("    [listen] " + connection.name() + ": " + event.getClass().getSimpleName()));
	}

	@Override
	public Mono<Void> streamEstablished(McpModernConnectionInfo connection) {
		return Mono.fromRunnable(() -> {
			System.out.println("    [listen] " + connection.name() + ": stream established");
			this.established.countDown();
		});
	}

	/**
	 * Waits for the first listen stream. The streams are opened in the background when the
	 * application starts, and a server that cannot be reached must not hold the start up.
	 */
	boolean awaitEstablished(Duration timeout) throws InterruptedException {
		return this.established.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
	}

}
