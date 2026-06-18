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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.mcp.client.autoconfigure.NamedClientMcpTransport;
import org.springframework.ai.mcp.client.autoconfigure.SseWebFluxTransportAutoConfiguration;
import org.springframework.ai.mcp.client.autoconfigure.properties.McpClientCommonProperties;
import org.springframework.ai.mcp.client.autoconfigure.properties.McpSseClientProperties;
import org.springframework.ai.mcp.client.autoconfigure.properties.McpSseClientProperties.SseParameters;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;



@Configuration
@ConditionalOnProperty(prefix = McpClientCommonProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true",
        matchIfMissing = true)
public class StreamableHttpClientTransportAutoConfiguration {


    @Bean("streamableTransport")
    public List<NamedClientMcpTransport> mcpHttpClientTransports(McpStreamableClientProperties sseProperties,
                                                                 ObjectProvider<ObjectMapper> objectMapperProvider) {

        ObjectMapper objectMapper = objectMapperProvider.getIfAvailable(ObjectMapper::new);

        List<NamedClientMcpTransport> sseTransports = new ArrayList<>();

        for (Map.Entry<String, McpStreamableClientProperties.StreamableParameters> serverParameters : sseProperties.getConnections().entrySet()) {

            String baseUrl = serverParameters.getValue().url();
            String sseEndpoint = serverParameters.getValue() != null
                    ? serverParameters.getValue().endpoint() : "/message";
            var transport = HttpClientStreamableHttpTransport.builder(baseUrl)
                    .endpoint(sseEndpoint)
                    .clientBuilder(HttpClient.newBuilder())
                    .objectMapper(objectMapper)
                    .build();
            sseTransports.add(new NamedClientMcpTransport(serverParameters.getKey(), transport));
        }

        return sseTransports;
    }

}
