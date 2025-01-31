/*
* Copyright 2024 - 2024 the original author or authors.
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
package org.springframework.ai.mcp.sample.client;

import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;

/**
 * With stdio transport, the MCP server is automatically started by the client. But you
 * have to build the server jar first:
 *
 * <pre>
 * ./mvnw clean install -DskipTests
 * </pre>
 */
public class ClientStdio {

	public static void main(String[] args) {

		var stdioParams = ServerParameters.builder("java")
			.args("-Dspring.ai.mcp.server.transport=STDIO", "-Dspring.main.web-application-type=none",
					"-Dlogging.pattern.console=", "-jar",
					"model-context-protocol/mcp-webflux-server-starter/target/mcp-webflux-server-starter-0.0.1-SNAPSHOT.jar")
			.build();

		new SampleClient(new StdioClientTransport(stdioParams)).run();
	}

}
