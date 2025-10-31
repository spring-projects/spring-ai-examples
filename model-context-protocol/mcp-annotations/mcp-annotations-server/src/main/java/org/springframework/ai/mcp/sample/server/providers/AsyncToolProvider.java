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
package org.springframework.ai.mcp.sample.server.providers;

import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springaicommunity.mcp.context.McpAsyncRequestContext;
import org.springaicommunity.mcp.context.StructuredElicitResult;
import reactor.core.publisher.Mono;

import org.springframework.stereotype.Service;

/**
 * @author Christian Tzolov
 */
@Service
public class AsyncToolProvider {

	// Note: Elicitation suppports subset of JSON Schema types. Use Number instead of int/Integer!
	public record Person(String name, Number age) {}

	@McpTool(description = "Test tool", name = "tool4", generateOutputSchema = true)
	public Mono<String> toolLggingSamplingElicitationProgress(McpAsyncRequestContext mcpCtx, @McpToolParam String input) {

		return Mono.defer(() -> Mono.just(input))
			// Initial logging and progress
			.doOnNext(i -> {
				mcpCtx.info("Tool4 Invoked with input: " + i);
				mcpCtx.progress(p -> p.progress(0.0).total(1.0).message("tool call start"));
				mcpCtx.ping();
			})
			// Execute elicitation
			.flatMap(i -> mcpCtx.elicit(Person.class)
				.doOnSuccess(result -> 
					mcpCtx.progress(p -> p.progress(0.50).total(1.0).message("elicitation completed"))
				)
				.map(elicitResult -> new Object[] { i, elicitResult })
			)
			// Execute sampling
			.flatMap(data -> {
				String originalInput = (String) data[0];
				StructuredElicitResult<Person> person = (StructuredElicitResult<Person>) data[1];
				
				return mcpCtx.sample(s -> s
					.message("Test Sampling Message")
					.maxTokens(500)
					.modelPreferences(mp -> mp.modelHints("OpenAi","Ollama")
							.costPriority(1.0)
							.speedPriority(1.0)
							.intelligencePriority(1.0)))
					.doOnSuccess(result -> 
						mcpCtx.progress(p -> p.progress(1.0).total(1.0).message("sampling completed"))
					)
					.map(samplingResult -> new Object[] { originalInput, person, samplingResult });
			})
			// Build final response
			.map(results -> {
				String originalInput = (String) results[0];
				StructuredElicitResult<Person> person = (StructuredElicitResult<Person>) results[1];
				CreateMessageResult samplingResult = (CreateMessageResult) results[2];
				
				String elicitResponse = person.structuredContent() != null ? person.structuredContent().toString() : "no elicitation response";
				String samplingResponse = samplingResult != null ? samplingResult.toString() : "no sample response";
				
				return String.format("CALL RESPONSE: %s, %s", samplingResponse, elicitResponse);
			})
			// Final logging
			.doOnSuccess(response -> mcpCtx.info("Tool4 Done"))
			.doOnError(error -> mcpCtx.info("Tool4 Error: " + error.getMessage()));
	}

}
