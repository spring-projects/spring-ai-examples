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

import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springaicommunity.mcp.context.McpSyncRequestContext;
import org.springaicommunity.mcp.context.StructuredElicitResult;

import org.springframework.stereotype.Service;

/**
 * @author Christian Tzolov
 */
@Service
public class ToolProvider3 {

	interface DanielsContext {
		McpSyncServerExchange exchange();
	}

	interface McpRoots {
		String roots();
	}

	public record Person(String name, Number age) {}

	@McpTool(description = "Test tool", name = "tool1", generateOutputSchema = true)
	public String toolLoggingSamplingElicitationProgress(McpSyncRequestContext ctx, @McpToolParam String input) {

		ctx.info("Tool Invoked"); // call client logging (info level)

		ctx.progress(p -> p.percentage(25).message("tool call start")); // call client progress

		ctx.ping(); // call client ping

		StructuredElicitResult<Person> elicitationResult = ctx.elicit(e -> e.message("Fill in"), Person.class);
			
			
		ctx.progress(p -> p.progress(0.50).total(1.0).message("elicitation completed"));
		
		CreateMessageResult samplingResponse = ctx.sample(s -> s
			.message("Test Sampling Message")
			.maxTokens(500)
			.modelPreferences(mp -> mp.modelHints("OpenAi","Ollama")
					.costPriority(1.0)
					.speedPriority(1.0)
					.intelligencePriority(1.0)));

		ctx.progress(p -> p.progress(1.0).total(1.0).message("sampling completed"));

		ctx.info("Tool2 Done");

		return "CALL RESPONSE: " + samplingResponse.toString() + ", " + elicitationResult.toString();
	}

}
