package com.example.tool.augment;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.ToolCallAdvisor;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.tool.augment.AugmentedToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

@SpringBootApplication
public class DemoApplication {

	private static final Logger logger = LoggerFactory.getLogger(DemoApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	public record AgentThinking(
			@ToolParam(description = "Your step-by-step reasoning for why you're calling this tool and what you expect",
					required = true) String innerThought,

			@ToolParam(description = "Confidence level (low, medium, high) in this tool choice",
					required = false) String confidence,

			@ToolParam(description = "Key insights to remember for future interactions",
					required = true) List<String> memoryNotes) {
	}

	public record MyResponse(String result, AgentThinking thinking) {
	}

	@Bean
	CommandLineRunner commandLineRunner(ChatClient.Builder chatClientBuilder) {
		return args -> {

			AugmentedToolCallbackProvider<AgentThinking> provider = AugmentedToolCallbackProvider
				.<AgentThinking>builder()
				.toolObject(new MyTools())
				.argumentType(AgentThinking.class)
				.argumentConsumer(event -> {
					// Access the extended arguments via event.arguments()
					AgentThinking thinking = event.arguments();

					// Log the LLM's reasoning - great for debugging and observability
					logger.info("LLM Reasoning: {}", thinking.innerThought());
					logger.info("Confidence: {}", thinking.confidence());
					logger.info("Thinking notest: {}", thinking.memoryNotes());

					// Access additional context from the event
					logger.info("Tool: {}", event.toolDefinition().name());
				})
				.removeExtraArgumentsAfterProcessing(true) // Remove before calling actual
															// tool
				.build();

			MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder().maxMessages(100).build();

			ChatClient chatClient = chatClientBuilder // @formatter:off
					.defaultToolCallbacks(provider)
					.defaultAdvisors(
						ToolCallAdvisor.builder()
							.advisorOrder(BaseAdvisor.HIGHEST_PRECEDENCE + 300)
							.conversationHistoryEnabled(false).build(),
						MessageChatMemoryAdvisor.builder(chatMemory).order(Ordered.HIGHEST_PRECEDENCE + 1000).build(),
						new MyLogAdvisor()) // order 0 = HIGHEST_PRECEDENCE + 2147483648
				.build();
				
				var answer = chatClient
					.prompt("What is current weather in Paris?")
					.call()
					.entity(MyResponse.class);

				// @formatter:on

			System.out.println(answer);
		};
	}

	static class MyTools {

		@Tool(description = "Get the current weather for a given location")
		public String weather(String location) {
			return "The current weather in " + location + " is sunny with a temperature of 25°C.";
		}

	}

	static class MyLogAdvisor implements BaseAdvisor {

		@Override
		public int getOrder() {
			return 0;
		}

		@Override
		public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
			print("REQUEST", chatClientRequest.prompt().getInstructions());
			return chatClientRequest;
		}

		@Override
		public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
			print("RESPONSE", chatClientResponse.chatResponse().getResults());
			return chatClientResponse;
		}

		private void print(String label, Object object) {
			System.out.println(label + ":" + ModelOptionsUtils.toJsonString(object) + "\n");
		}

	}

}
