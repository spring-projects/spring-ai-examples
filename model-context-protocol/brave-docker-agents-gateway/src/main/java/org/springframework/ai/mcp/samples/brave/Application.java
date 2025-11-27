package org.springframework.ai.mcp.samples.brave;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

	private static final Logger logger = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args).close();
	}

	@Bean
	public CommandLineRunner predefinedQuestions(ChatClient.Builder chatClientBuilder,
			ToolCallbackProvider toolCallbackProvider) {

		return args -> {

			var chatClient = chatClientBuilder.defaultToolCallbacks(toolCallbackProvider).build();

			String question = "Does Spring AI supports the Model Context Protocol? Please provide some references.";
			logger.info("QUESTION: {}\n", question);
			logger.info("ASSISTANT: {}\n", chatClient.prompt(question).call().content());
		};
	}

}