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

package org.springframework.ai.mcp.samples.client.customizers;

import java.util.List;

import org.springaicommunity.mcp.method.elicitation.SyncElicitationSpecification;
import org.springaicommunity.mcp.method.logging.SyncLoggingSpecification;
import org.springaicommunity.mcp.method.progress.SyncProgressSpecification;
import org.springaicommunity.mcp.method.sampling.SyncSamplingSpecification;
import org.springframework.ai.mcp.customizer.McpSyncClientCustomizer;

import io.modelcontextprotocol.client.McpClient.SyncSpec;

public class AnnotationSyncClientCustomizer implements McpSyncClientCustomizer {

	private final List<SyncSamplingSpecification> syncSamplingSpecifications;
	private final List<SyncLoggingSpecification> syncLoggingSpecifications;
	private final List<SyncElicitationSpecification> syncElicitationSpecifications;
	private final List<SyncProgressSpecification> syncProgressSpecifications;

	public AnnotationSyncClientCustomizer(List<SyncSamplingSpecification> syncSamplingSpecifications,
			List<SyncLoggingSpecification> syncLoggingSpecifications,
			List<SyncElicitationSpecification> syncElicitationSpecifications,
			List<SyncProgressSpecification> syncProgressSpecifications) {

		this.syncSamplingSpecifications = syncSamplingSpecifications;
		this.syncLoggingSpecifications = syncLoggingSpecifications;
		this.syncElicitationSpecifications = syncElicitationSpecifications;
		this.syncProgressSpecifications = syncProgressSpecifications;
	}

	@Override
	public void customize(String name, SyncSpec clientSpec) {

		this.syncSamplingSpecifications.forEach(samplingSpec -> {
			if (samplingSpec.clientId().isEmpty() || samplingSpec.clientId().equals(name)) {
				clientSpec.sampling(samplingSpec.samplingHandler());
			}
		});

		this.syncLoggingSpecifications.forEach(loggingSpec -> {
			if (loggingSpec.clientId().isEmpty() || loggingSpec.clientId().equals(name)) {
				clientSpec.loggingConsumer(loggingSpec.loggingHandler());
			}
		});

		this.syncElicitationSpecifications.forEach(elicitationSpec -> {
			if (elicitationSpec.clientId().isEmpty() || elicitationSpec.clientId().equals(name)) {
				clientSpec.elicitation(elicitationSpec.elicitationHandler());
			}
		});

		this.syncProgressSpecifications.forEach(progressSpec -> {
			if (progressSpec.clientId().isEmpty() || progressSpec.clientId().equals(name)) {
				clientSpec.progressConsumer(progressSpec.progressHandler());
			}
		});
	}

}
