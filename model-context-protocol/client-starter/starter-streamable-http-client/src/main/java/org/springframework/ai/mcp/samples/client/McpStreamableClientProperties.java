/*
 * Copyright 2025-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ai.mcp.samples.client;

import org.springframework.ai.mcp.client.autoconfigure.properties.McpSseClientProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for Streamable Http based MCP client connections.
 *
 * <p>
 * These properties allow configuration of multiple named streamable HTTP connections to MCP servers.
 * Each connection is configured with a URL and endpoint for streamable HTTP communication.
 *
 * <p>
 * Example configuration: <pre>
 * spring.ai.mcp.client.streamable:
 *   connections:
 *     server1:
 *       url: http://localhost:8080
 *       endpoint: /mcp
 *     server2:
 *       url: http://otherserver:8081
 *       endpoint: /mcp
 * </pre>
 *
 * @author Dafu Wnag
 * @since 1.0.0
 * @see
 */
@ConfigurationProperties(McpStreamableClientProperties.CONFIG_PREFIX)
public class McpStreamableClientProperties {

	public static final String CONFIG_PREFIX = "spring.ai.mcp.client.streamable";

	/**
	 * Map of named streamable HTTP connection configurations.
	 * <p>
	 * The key represents the connection name, and the value contains the streamable HTTP parameters
	 * for that connection.
	 */
	private final Map<String, StreamableParameters> connections = new HashMap<>();

	/**
	 * Returns the map of configured streamable HTTP connections.
	 * @return map of connection names to their streamable HTTP parameters
	 */
	public Map<String, StreamableParameters> getConnections() {
		return this.connections;
	}

	/**
	 * Parameters for configuring a streamable HTTP connection to an MCP server.
	 *
	 * @param url the base URL for streamable HTTP communication with the MCP server
	 * @param endpoint the endpoint path for the MCP server
	 */
	public record StreamableParameters(String url, String endpoint) {
	}

}
