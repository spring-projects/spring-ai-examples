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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Configuration;
import org.springaicommunity.mcp.method.elicitation.SyncElicitationSpecification;
import org.springaicommunity.mcp.method.logging.SyncLoggingSpecification;
import org.springaicommunity.mcp.method.progress.SyncProgressSpecification;
import org.springaicommunity.mcp.method.sampling.SyncSamplingSpecification;
import org.springaicommunity.mcp.spring.SyncMcpAnnotationProvider;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.customizer.McpSyncClientCustomizer;
import org.springframework.ai.mcp.samples.client.customizers.AnnotationSyncClientCustomizer;
import org.springframework.context.annotation.Bean;

@Configuration
public class AppConfiguration {

	@Bean
	List<SyncLoggingSpecification> loggingSpecs(McpClientHandlers clientMcpHandlers) {
		return SyncMcpAnnotationProvider.createSyncLoggingSpecifications(List.of(clientMcpHandlers));
	}

	@Bean
	List<SyncSamplingSpecification> samplingSpecs(McpClientHandlers clientMcpHandlers) {
		return SyncMcpAnnotationProvider.createSyncSamplingSpecifications(List.of(clientMcpHandlers));
	}

	@Bean
	List<SyncElicitationSpecification> elicitationSpecs(McpClientHandlers clientMcpHandlers) {
		return SyncMcpAnnotationProvider.createSyncElicitationSpecifications(List.of(clientMcpHandlers));
	}

	@Bean
	List<SyncProgressSpecification> progressSpecs(McpClientHandlers clientMcpHandlers) {
		return SyncMcpAnnotationProvider.createSyncProgressSpecifications(List.of(clientMcpHandlers));
	}

	@Bean
	McpSyncClientCustomizer annotationMcpSyncClientCustomizer(List<SyncLoggingSpecification> loggingSpecs,
			List<SyncSamplingSpecification> samplingSpecs, List<SyncElicitationSpecification> elicitationSpecs,
			List<SyncProgressSpecification> progressSpecs) {
		return new AnnotationSyncClientCustomizer(samplingSpecs, loggingSpecs, elicitationSpecs, progressSpecs);
	}

	@Bean
	public Map<String, ChatClient> chatClients(List<ChatModel> chatModels) {
		return chatModels.stream().collect(Collectors.toMap(model -> model.getClass().getSimpleName().toLowerCase(),
				model -> ChatClient.builder(model).build()));

	}
}