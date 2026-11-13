package com.example.tickets.client;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.modelcontextprotocol.modern.client.SubscriptionEvent;

import org.springframework.ai.mcp.modern.client.McpModernConnectionInfo;
import org.springframework.ai.mcp.modern.client.SyncModernMcpChangeListener;

/**
 * Prints what the listen streams of the modern connections report. A
 * {@link SyncModernMcpChangeListener} bean hears it next to the tool callback provider, which
 * drops a kept tool listing, and the {@code @McpToolListChanged} methods.
 */
class ListenLog implements SyncModernMcpChangeListener {

	private final CountDownLatch established = new CountDownLatch(1);

	@Override
	public void changed(McpModernConnectionInfo connection, SubscriptionEvent event) {
		System.out.println("    [listen] " + connection.name() + ": " + event.getClass().getSimpleName());
	}

	@Override
	public void streamEstablished(McpModernConnectionInfo connection) {
		System.out.println("    [listen] " + connection.name() + ": stream established");
		this.established.countDown();
	}

	/**
	 * Waits for the first listen stream. The streams are opened in the background when the
	 * application starts, and a server that cannot be reached must not hold the start up.
	 */
	boolean awaitEstablished(Duration timeout) throws InterruptedException {
		return this.established.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
	}

}
