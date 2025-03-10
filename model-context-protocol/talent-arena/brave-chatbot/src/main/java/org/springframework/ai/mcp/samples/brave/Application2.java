package org.springframework.ai.mcp.samples.brave;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application2 {

	public static void main(String[] args) {
		SpringApplication.run(Application2.class, args);
	}

@Bean
public CommandLineRunner chatbot(ChatClient.Builder chatClientBuilder, ToolCallbackProvider tools) {

	return args -> {						
		var output = chatClientBuilder.build().prompt()
				.system("You are useful assistant and can perform web searches Brave's search API to reply to your questions.")
				.user("Create a summary about the Talent Arena conference and save it as markdown talent-arena.md file.")
				.tools(tools)
				.call()
				.content();
	};
}
}