
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
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

// ------------------------------------------------------------
// ROUTER WORKFLOW
// ------------------------------------------------------------
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ChatClient.Builder chatClientBuilder) {
		
		return args -> {
			Map<String, String> routeMap = Map.of(
					"billing", "Billing and money related problem handling",
					"technical", "Technical level problem handling",
					"account", "Account security related problem handling",
					"product", "Product and feature related problem handling"
			);

			Map<String, String> promptMap = Map.of("billing",
					"""
							You are a billing support specialist. Follow these guidelines:
							1. Always start with "Billing Support Response:"
							2. First acknowledge the specific billing issue
							3. Explain any charges or discrepancies clearly
							4. List concrete next steps with timeline
							5. End with payment options if relevant

							Keep responses professional but friendly.

							Input: """,

					"technical",
					"""
							You are a technical support engineer. Follow these guidelines:
							1. Always start with "Technical Support Response:"
							2. List exact steps to resolve the issue
							3. Include system requirements if relevant
							4. Provide workarounds for common problems
							5. End with escalation path if needed

							Use clear, numbered steps and technical details.

							Input: """,

					"account",
					"""
							You are an account security specialist. Follow these guidelines:
							1. Always start with "Account Support Response:"
							2. Prioritize account security and verification
							3. Provide clear steps for account recovery/changes
							4. Include security tips and warnings
							5. Set clear expectations for resolution time

							Maintain a serious, security-focused tone.

							Input: """,

					"product",
					"""
							You are a product specialist. Follow these guidelines:
							1. Always start with "Product Support Response:"
							2. Focus on feature education and best practices
							3. Include specific examples of usage
							4. Link to relevant documentation sections
							5. Suggest related features that might help

							Be educational and encouraging in tone.

							Input: """);

			List<String> tickets = List.of(
					"""
							Subject: Can't access my account
							Message: Hi, I've been trying to log in for the past hour but keep getting an 'invalid password' error.
							I'm sure I'm using the right password. Can you help me regain access? This is urgent as I need to
							submit a report by end of day.
							- John""",

					"""
							Subject: Unexpected charge on my card
							Message: Hello, I just noticed a charge of .99 on my credit card from your company, but I thought
							I was on the .99 plan. Can you explain this charge and adjust it if it's a mistake?
							Thanks,
							Sarah""",

					"""
							Subject: How to export data?
							Message: I need to export all my project data to Excel. I've looked through the docs but can't
							figure out how to do a bulk export. Is this possible? If so, could you walk me through the steps?
							Best regards,
							Mike""");

			// Select a proper chat client for responses
			ChatClient chatClient = chatClientBuilder.build();

			// Select a proper chat client for routing task
			ChatClient routingChatClient = chatClientBuilder.build();

			var routerWorkflow = new RoutingWorkflow(routingChatClient);

			int i = 1;
			for (String ticket : tickets) {
				System.out.println("\nTicket " + i++);
				System.out.println("------------------------------------------------------------");
				System.out.println(ticket);
				System.out.println("------------------------------------------------------------");
				String route = routerWorkflow.route(ticket, routeMap);
				String prompt = promptMap.get(route);

				String response = chatClient.prompt(prompt + "\nInput: " + ticket).call().content();
				System.out.println(response);
			}

		};
	}
}
