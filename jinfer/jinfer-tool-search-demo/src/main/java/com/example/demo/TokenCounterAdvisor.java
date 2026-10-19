/*
* Copyright 2026 - 2026 the original author or authors.
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
package com.example.demo;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.core.Ordered;

/**
 * @author Christian Tzolov
 */

public class TokenCounterAdvisor implements BaseAdvisor {

	private static final String PURPLE = "\033[38;5;129m";

	private static final String RESET = "\033[0m";

	AtomicInteger totalTokenCouner = new AtomicInteger(0);

	AtomicInteger promptTokenCouner = new AtomicInteger(0);

	AtomicInteger completionTokenCouner = new AtomicInteger(0);

	AtomicInteger requestCouner = new AtomicInteger(0);

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE - 1000;
	}

	@Override
	public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
		return chatClientRequest;
	}

	@Override
	public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
		var usage = chatClientResponse.chatResponse().getMetadata().getUsage();

		requestCouner.incrementAndGet();
		totalTokenCouner.addAndGet(usage.getTotalTokens());
		promptTokenCouner.addAndGet(usage.getPromptTokens());
		completionTokenCouner.addAndGet(usage.getCompletionTokens());

		System.out.println(PURPLE + "Current TOKENS Total: " + usage.getTotalTokens() + ", Completion: "
				+ usage.getCompletionTokens() + ", Prompt: " + usage.getPromptTokens() + RESET);

		System.out.println(
				PURPLE + "Accumulated TOKENS Total: " + totalTokenCouner.get() + ", Completion: " + completionTokenCouner.get()
						+ ", Prompt: " + promptTokenCouner.get() + ", Number of requests: " + requestCouner.get() + RESET);

		return chatClientResponse;
	}

}
