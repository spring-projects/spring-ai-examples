/*
 * Copyright 2025 - 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ai.mcp.sample.server;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import io.modelcontextprotocol.json.McpJsonDefaults;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import static java.net.http.HttpRequest.BodyPublishers.ofString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * @author Daniel Garnier-Moiroux
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "server.shutdown=immediate")
class McpServerApplicationTests {

	@LocalServerPort
	private int port;

	private final JsonMapper jsonMapper = new JsonMapper();
	
	@Test
	void whenTokenThenConnects() throws IOException, InterruptedException {
		var accessToken = obtainAccessToken();

		var client = HttpClient.newHttpClient();

		var request = HttpRequest.newBuilder()
			.uri(URI.create("http://localhost:" + port + "/sse"))
			.header("Accept", "text/event-stream")
			.header("Authorization", "Bearer " + accessToken)
			.GET()
			.build();

		var responseCode = new AtomicInteger(-1);
		var sseRequest = client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream()).thenApply(response -> {
			responseCode.set(response.statusCode());
			if (response.statusCode() == 200) {
				return response;
			}
			else {
				throw new RuntimeException("Failed to connect to SSE endpoint: " + response.statusCode());
			}
		});

		await().atMost(Duration.ofSeconds(1)).until(sseRequest::isDone);
		assertThat(sseRequest).isCompleted();
		assertThat(responseCode).hasValue(200);
	}

	@Test
	void whenNoTokenThenFails() throws IOException, InterruptedException {
		var client = HttpClient.newHttpClient();
		var request = HttpRequest.newBuilder()
			.uri(URI.create("http://localhost:" + port + "/sse"))
			.header("Accept", "text/event-stream")
			.GET()
			.build();

		var response = client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
		assertThat(response).isEqualTo(401);
	}

	private String obtainAccessToken() throws IOException, InterruptedException {
		var client = HttpClient.newHttpClient();

		var clearTextCredentials = "mcp-client:secret".getBytes(StandardCharsets.UTF_8);
		var credentials = new String(Base64.getUrlEncoder().encode(clearTextCredentials));
		var request = HttpRequest.newBuilder()
			.uri(URI.create("http://localhost:" + port + "/oauth2/token"))
			.header("Authorization", "Basic " + credentials)
			.header("Content-Type", "application/x-www-form-urlencoded")
			.POST(ofString("grant_type=client_credentials"))
			.build();

		var rawResponse = client.send(request, HttpResponse.BodyHandlers.ofString()).body();

		Map<String, String> response = jsonMapper.readValue(rawResponse, Map.class);
		return response.get("access_token");
	}

}
