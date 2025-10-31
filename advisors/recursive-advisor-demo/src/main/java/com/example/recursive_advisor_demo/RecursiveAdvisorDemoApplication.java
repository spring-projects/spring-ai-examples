package com.example.recursive_advisor_demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.ToolCallAdvisor;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RecursiveAdvisorDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(RecursiveAdvisorDemoApplication.class, args);
	}

	@Bean
	CommandLineRunner commandLineRunner(ChatClient.Builder chatClientBuilder) {
		return args -> {

			ChatClient chatClient = chatClientBuilder // @formatter:off
					.defaultTools(new MyTools())
					.defaultAdvisors(
						ToolCallAdvisor.builder().build(),
						new MyLogAdvisor())
				.build(); 
				
				var answer = chatClient
					.prompt("What is current weather in Paris?")
					.call()
					.content();

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
