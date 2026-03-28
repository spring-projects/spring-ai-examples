package com.example.jamjet_durable_agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Demonstrates JamJet durable execution with Spring AI.
 *
 * <p>By adding {@code jamjet-spring-boot-starter} to the classpath,
 * every {@link ChatClient} call automatically gains:
 * <ul>
 *   <li>Crash recovery — resume from last checkpoint</li>
 *   <li>Event-sourced audit trails — every prompt, response, and tool call logged</li>
 *   <li>Execution tracking — workflow state visible via JamJet runtime API</li>
 * </ul>
 *
 * <p><strong>No code changes needed.</strong> The {@code JamjetDurabilityAdvisor}
 * is auto-injected via Spring AI's {@code ChatClientCustomizer}.
 *
 * <h2>Running</h2>
 * <pre>{@code
 * # 1. Start JamJet runtime
 * docker run -p 7700:7700 ghcr.io/jamjet-labs/jamjet:latest
 *
 * # 2. Run the app
 * OPENAI_API_KEY=sk-... mvn spring-boot:run
 * }</pre>
 */
@SpringBootApplication
public class JamjetDurableAgentDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(JamjetDurableAgentDemoApplication.class, args);
	}

	@Bean
	CommandLineRunner demo(ChatClient.Builder chatClientBuilder) {
		return args -> {
			// JamjetDurabilityAdvisor + JamjetAuditAdvisor are auto-injected
			ChatClient chatClient = chatClientBuilder
					.defaultTools(new ResearchTools())
					.build();

			// This call is now durable:
			// - Workflow created on JamJet runtime
			// - Execution started and tracked
			// - Tool calls recorded in audit trail
			// - If the app crashes, execution resumes from last checkpoint
			String answer = chatClient
					.prompt("What is the weather in Tokyo and what should I wear?")
					.call()
					.content();

			System.out.println("\n--- Agent Response ---");
			System.out.println(answer);
			System.out.println("--- Durable execution complete ---");
		};
	}

	static class ResearchTools {

		@Tool(description = "Get the current weather for a given city")
		public String getWeather(String city) {
			// Simulated weather API — in production this would call a real service.
			// JamJet records this tool call in the audit trail.
			return "The weather in " + city + " is 22°C, partly cloudy with light winds.";
		}

		@Tool(description = "Get clothing recommendations for given weather conditions")
		public String getClothingAdvice(String conditions) {
			return "For " + conditions + ": light layers, comfortable shoes, and bring a light jacket.";
		}
	}
}
