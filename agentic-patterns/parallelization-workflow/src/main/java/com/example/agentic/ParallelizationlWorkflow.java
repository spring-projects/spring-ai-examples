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
package com.example.agentic;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.util.Assert;

/**
 * Implements the Parallelization Workflow pattern for efficient concurrent processing
 * of multiple LLM operations. This pattern enables parallel execution of LLM calls
 * with automated output aggregation, significantly improving throughput for
 * batch processing scenarios.
 * 
 * <p>The pattern manifests in two key variations:</p>
 * 
 * <ul>
 * <li><b>Sectioning</b>: Decomposes a complex task into independent subtasks that
 * can be processed concurrently. For example, analyzing different sections of a
 * document simultaneously.</li>
 * <li><b>Voting</b>: Executes identical prompts multiple times in parallel to
 * gather diverse perspectives or implement majority voting mechanisms. This is
 * particularly useful for validation or consensus-building tasks.</li>
 * </ul>
 *
 * <p><b>Key Benefits:</b></p>
 * <ul>
 * <li>Improved throughput through concurrent processing</li>
 * <li>Better resource utilization of LLM API capacity</li>
 * <li>Reduced overall processing time for batch operations</li>
 * <li>Enhanced result quality through multiple perspectives (in voting scenarios)</li>
 * </ul>
 *
 * <p><b>When to Use:</b></p>
 * <ul>
 * <li>Processing large volumes of similar but independent items</li>
 * <li>Tasks requiring multiple independent perspectives or validations</li>
 * <li>Scenarios where processing time is critical and tasks are parallelizable</li>
 * <li>Complex operations that can be decomposed into independent subtasks</li>
 * </ul>
 *
 * <p><b>Implementation Considerations:</b></p>
 * <ul>
 * <li>Ensure tasks are truly independent to avoid consistency issues</li>
 * <li>Consider API rate limits when determining parallel execution capacity</li>
 * <li>Monitor resource usage (memory, CPU) when scaling parallel operations</li>
 * <li>Implement appropriate error handling for parallel task failures</li>
 * </ul>
 *
 * @author Christian Tzolov
 * @see org.springframework.ai.chat.client.ChatClient
 * @see <a href="https://docs.spring.io/spring-ai/reference/1.0/api/chatclient.html">Spring AI ChatClient</a>
 * @see <a href=
 *      "https://www.anthropic.com/research/building-effective-agents">Building
 *      Effective Agents</a>
 */
public class ParallelizationlWorkflow {

	private final ChatClient chatClient;

	public ParallelizationlWorkflow(ChatClient chatClient) {
		this.chatClient = chatClient;
	}

	/**
	 * Processes multiple inputs concurrently using a fixed thread pool and the same prompt template.
	 * This method maintains the order of results corresponding to the input order.
	 *
	 * @param prompt   The prompt template to use for each input. The input will be appended to this prompt.
	 *                 Must not be null. Example: "Translate the following text to French:"
	 * @param inputs   List of input strings to process. Each input will be processed independently
	 *                 in parallel. Must not be null or empty. Example: ["Hello", "World", "Good morning"]
	 * @param nWorkers The number of concurrent worker threads to use. This controls the maximum
	 *                 number of simultaneous LLM API calls. Must be greater than 0. Consider API
	 *                 rate limits when setting this value.
	 * @return List of processed results in the same order as the inputs. Each result contains
	 *         the LLM's response for the corresponding input.
	 * @throws IllegalArgumentException if prompt is null, inputs is null/empty, or nWorkers <= 0
	 * @throws RuntimeException if processing fails for any input, with the cause containing
	 *         the specific error details
	 */
	public List<String> parallel(String prompt, List<String> inputs, int nWorkers) {
		Assert.notNull(prompt, "Prompt cannot be null");
		Assert.notEmpty(inputs, "Inputs list cannot be empty");
		Assert.isTrue(nWorkers > 0, "Number of workers must be greater than 0");

		ExecutorService executor = Executors.newFixedThreadPool(nWorkers);
		try {
			List<CompletableFuture<String>> futures = inputs.stream()
					.map(input -> CompletableFuture.supplyAsync(() -> {
						try {
							return chatClient.prompt(prompt + "\nInput: " + input).call().content();
						} catch (Exception e) {
							throw new RuntimeException("Failed to process input: " + input, e);
						}
					}, executor))
					.collect(Collectors.toList());

			// Wait for all tasks to complete
			CompletableFuture<Void> allFutures = CompletableFuture.allOf(
					futures.toArray(CompletableFuture[]::new));
			allFutures.join();

			return futures.stream()
					.map(CompletableFuture::join)
					.collect(Collectors.toList());

		} finally {
			executor.shutdown();
		}
	}

}
