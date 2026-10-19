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

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.MemoryAdvisor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

@SpringBootApplication
public class TsTAutoconfDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(TsTAutoconfDemoApplication.class, args);
	}

	// tool-index-type=vector: the tool descriptions are embedded by the local jinfer
	// EmbeddingModel and searched in this in-memory store.
	@Bean
	VectorStore vectorStore(EmbeddingModel embeddingModel) {
		return SimpleVectorStore.builder(embeddingModel).build();
	}

	@Bean
	public CommandLineRunner cli(ChatClient.Builder chatClientBuilder) {
		return args -> { // @formatter:off

			var loggingAdvisor = MyLoggingAdvisor.builder()
					.order(Ordered.HIGHEST_PRECEDENCE + 2000)
					.showAvailableTools(true)
					.build();

			ChatClient chatClient = chatClientBuilder
				.defaultTools(new MyTools(), new DummyTools())
				.defaultAdvisors(loggingAdvisor)
				.defaultAdvisors(new TokenCounterAdvisor())
				.defaultAdvisors(a -> a.param(MemoryAdvisor.CONVERSATION_ID, "chat_memory_conversation_id"))
				.build();
			var answer = chatClient.prompt("""
					Help me plan what to wear today in Landsmeer, NL.
					Please suggest clothing shops that are open right now in the area.

					Do not make assumptions about the date, time. Use the tools for getting the current time.
					""")
					.call().content();

			System.out.println(answer);

		}; // @formatter:on
	}

	static class MyTools {

		@Tool(description = "Get the weather for a given location and at a given time")
		public String weather(String location, @ToolParam(description = "YYYY-MM-DDTHH:mm:ss") String atTime) {
			return "The current weather in " + location + " is sunny with a temperature of 25°C.";
		}

		@Tool(description = "Get the names of clothing shops in a location that are open at a given time")
		public List<String> clothing(String location,
				@ToolParam(description = "YYYY-MM-DDTHH:mm:ss") String openAtTime) {
			return List.of("Foo", "Bar", "Baz");
		}

		@Tool(description = "Provides the current date and time (as date-time string) for a given location")
		public String currentTime(String location) {
			return LocalDateTime.now().toString();
		}

	}

}
