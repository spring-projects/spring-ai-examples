package com.example.tickets.client;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;

import tools.jackson.databind.json.JsonMapper;

/**
 * A minimal client for the 2026-07-28 protocol, enough to drive the sample servers. There is
 * no modern MCP Java client in the SDK yet, so this shows the wire format directly: one POST
 * per request, {@code _meta} with protocol version and client capabilities, the
 * {@code Mcp-Method}/{@code MCP-Protocol-Version}/{@code Mcp-Name} headers, JSON or SSE
 * responses, and the multi round-trip retry loop.
 */
public class ModernMcpDriver {

	public static final String PROTOCOL_VERSION = "2026-07-28";

	private static final JsonMapper JSON = JsonMapper.builder().build();

	private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

	/** What this driver declares by default: it can answer form elicitation and sampling. */
	public static final Map<String, Object> DEFAULT_CAPABILITIES = Map.of("elicitation", Map.of("form", Map.of()),
			"sampling", Map.of());

	private final URI endpoint;

	private final Map<String, String> headers;

	private final Map<String, Object> capabilities;

	private final AtomicLong ids = new AtomicLong();

	public ModernMcpDriver(String endpoint) {
		this(URI.create(endpoint), Map.of(), DEFAULT_CAPABILITIES);
	}

	private ModernMcpDriver(URI endpoint, Map<String, String> headers, Map<String, Object> capabilities) {
		this.endpoint = endpoint;
		this.headers = headers;
		this.capabilities = capabilities;
	}

	/** A driver that sends an extra HTTP header on every request, e.g. {@code X-Role: admin}. */
	public ModernMcpDriver withHeader(String name, String value) {
		Map<String, String> headers = new LinkedHashMap<>(this.headers);
		headers.put(name, value);
		return new ModernMcpDriver(this.endpoint, headers, this.capabilities);
	}

	/**
	 * A driver that declares other client capabilities. There is no handshake: capabilities
	 * travel in {@code _meta} of every request, and a tool that needs one the request does not
	 * declare is refused with {@code -32021}.
	 */
	public ModernMcpDriver withCapabilities(Map<String, Object> capabilities) {
		return new ModernMcpDriver(this.endpoint, this.headers, capabilities);
	}

	public Map<String, Object> discover() throws Exception {
		return call("server/discover", Map.of(), null, Map.of());
	}

	public Map<String, Object> listTools() throws Exception {
		return call("tools/list", Map.of(), null, Map.of());
	}

	public Map<String, Object> listPrompts() throws Exception {
		return call("prompts/list", Map.of(), null, Map.of());
	}

	public Map<String, Object> listResources(String cursor) throws Exception {
		return call("resources/list", cursor != null ? Map.of("cursor", cursor) : Map.of(), null, Map.of());
	}

	public Map<String, Object> readResource(String uri) throws Exception {
		return call("resources/read", Map.of("uri", uri), uri, Map.of());
	}

	public Map<String, Object> getPrompt(String name, Map<String, Object> arguments) throws Exception {
		return call("prompts/get", Map.of("name", name, "arguments", arguments), name, Map.of());
	}

	/**
	 * Calls a tool and, while the server answers {@code input_required}, answers the input
	 * requests with {@code answers} and retries with the sealed {@code requestState}.
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> callTool(String name, Map<String, Object> arguments, InputAnswerer answers,
			Consumer<Map<String, Object>> onNotification) throws Exception {
		Map<String, Object> params = new LinkedHashMap<>(Map.of("name", name, "arguments", arguments));
		Map<String, Object> extraMeta = Map.of("progressToken", "tok-" + name);
		for (int round = 1; round <= 5; round++) {
			Map<String, Object> result = callStreaming("tools/call", params, name, extraMeta, onNotification);
			if (!"input_required".equals(result.get("resultType"))) {
				return result;
			}
			Map<String, Object> inputRequests = (Map<String, Object>) result.get("inputRequests");
			Map<String, Object> responses = new LinkedHashMap<>();
			inputRequests.forEach((key, request) -> responses.put(key, answers.answer(key, (Map<String, Object>) request)));
			System.out.println("    input_required round " + round + ", answering " + responses.keySet());
			params = new LinkedHashMap<>(params);
			params.put("inputResponses", responses);
			params.put("requestState", result.get("requestState"));
		}
		throw new IllegalStateException("Too many input_required rounds for " + name);
	}

	/**
	 * Opens {@code subscriptions/listen} and hands every event to the consumer until the
	 * returned handle is closed. {@code notifications} is the subscription filter, for example
	 * {@code {"toolsListChanged": true}} or {@code {"resourceSubscriptions": ["tickets://t-1"]}}.
	 */
	public AutoCloseable listen(Map<String, Object> notifications, Consumer<Map<String, Object>> onEvent) {
		AtomicReference<Stream<String>> body = new AtomicReference<>();
		Thread thread = new Thread(() -> {
			try {
				Map<String, Object> params = Map.of("notifications", notifications, "_meta", meta(Map.of()));
				HttpRequest request = request("subscriptions/listen", null, params);
				HttpResponse<Stream<String>> response = this.http.send(request, HttpResponse.BodyHandlers.ofLines());
				body.set(response.body());
				response.body().filter(line -> line.startsWith("data:")).forEach(line -> onEvent.accept(parse(sseData(line))));
			}
			catch (IOException | InterruptedException | UncheckedIOException ex) {
				// stream closed
			}
		}, "mcp-listen");
		thread.setDaemon(true);
		thread.start();
		return () -> {
			// Closing the body closes the connection, which ends the server's listen stream.
			Stream<String> stream = body.get();
			if (stream != null) {
				stream.close();
			}
			thread.join(1000);
		};
	}

	private Map<String, Object> call(String method, Map<String, Object> params, String mcpName, Map<String, Object> extraMeta)
			throws Exception {
		return callStreaming(method, params, mcpName, extraMeta, n -> { });
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> callStreaming(String method, Map<String, Object> params, String mcpName,
			Map<String, Object> extraMeta, Consumer<Map<String, Object>> onNotification) throws Exception {
		Map<String, Object> withMeta = new LinkedHashMap<>(params);
		withMeta.put("_meta", meta(extraMeta));
		HttpResponse<String> response = this.http.send(request(method, mcpName, withMeta), HttpResponse.BodyHandlers.ofString());
		String contentType = response.headers().firstValue("Content-Type").orElse("");
		Map<String, Object> last = null;
		if (contentType.startsWith("text/event-stream")) {
			for (String line : response.body().split("\n")) {
				if (line.startsWith("data:")) {
					Map<String, Object> message = parse(sseData(line));
					if (message.containsKey("method")) {
						onNotification.accept(message);
					}
					else {
						last = message;
					}
				}
			}
		}
		else {
			last = parse(response.body());
		}
		if (last == null) {
			throw new IllegalStateException("No response for " + method + " (HTTP " + response.statusCode() + ")");
		}
		if (last.get("error") instanceof Map<?, ?> error) {
			throw new McpError(((Number) error.get("code")).intValue(), String.valueOf(error.get("message")), error.get("data"));
		}
		return (Map<String, Object>) last.get("result");
	}

	private HttpRequest request(String method, String mcpName, Map<String, Object> params) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("jsonrpc", "2.0");
		body.put("id", this.ids.incrementAndGet());
		body.put("method", method);
		body.put("params", params);
		HttpRequest.Builder builder = HttpRequest.newBuilder(this.endpoint)
			.header("Content-Type", "application/json")
			.header("Accept", "application/json, text/event-stream")
			.header("Mcp-Method", method)
			.header("MCP-Protocol-Version", PROTOCOL_VERSION)
			.POST(HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(body)));
		if (mcpName != null) {
			builder.header("Mcp-Name", mcpName); // tools/call, prompts/get, resources/read
		}
		this.headers.forEach(builder::header);
		return builder.build();
	}

	private Map<String, Object> meta(Map<String, Object> extra) {
		Map<String, Object> meta = new LinkedHashMap<>();
		meta.put("io.modelcontextprotocol/protocolVersion", PROTOCOL_VERSION);
		meta.put("io.modelcontextprotocol/clientCapabilities", this.capabilities);
		meta.putAll(extra);
		return meta;
	}

	/** The payload of an SSE data line; the space after the colon is optional. */
	private static String sseData(String line) {
		return line.substring("data:".length()).trim();
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> parse(String json) {
		return JSON.readValue(json, Map.class);
	}

	/** A JSON-RPC error answered by the server. */
	public static class McpError extends RuntimeException {

		private final int code;

		private final Object data;

		McpError(int code, String message, Object data) {
			super(code + " " + message + (data != null ? " " + data : ""));
			this.code = code;
			this.data = data;
		}

		public int code() {
			return this.code;
		}

		public Object data() {
			return this.data;
		}

	}

	/** Produces the {@code inputResponses[key]} value for one input request. */
	public interface InputAnswerer {

		Object answer(String key, Map<String, Object> inputRequest);

	}

	/**
	 * Answers the input requests a server may return: elicitations with an accepting form,
	 * sampling with a canned text, roots with an empty list. Each input request carries the
	 * method the server would have called on a session-based client.
	 */
	public static InputAnswerer autoAnswer() {
		return (key, request) -> {
			String method = String.valueOf(request.get("method"));
			switch (method) {
				case "elicitation/create":
					return Map.of("action", "accept", "content", Map.of("confirmed", true));
				case "sampling/createMessage":
					return Map.of("role", "assistant", "content", Map.of("type", "text", "text", "Two tickets are open."),
							"model", "demo-model");
				case "roots/list":
					return Map.of("roots", List.of());
				default:
					throw new IllegalArgumentException("Unknown input request " + method + " for key " + key);
			}
		};
	}

	public static List<String> names(Map<String, Object> listResult, String field) {
		return values(listResult, field, "name");
	}

	public static List<String> values(Map<String, Object> listResult, String field, String key) {
		List<String> values = new ArrayList<>();
		for (Object item : (List<?>) listResult.get(field)) {
			values.add(String.valueOf(((Map<?, ?>) item).get(key)));
		}
		return values;
	}

}
