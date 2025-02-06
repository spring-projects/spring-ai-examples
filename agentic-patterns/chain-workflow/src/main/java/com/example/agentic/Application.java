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

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

// ------------------------------------------------------------
// PROMPT CHAINING WORKFLOW
// ------------------------------------------------------------

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	String report = """
			Q3 Performance Summary:
			Our customer satisfaction score rose to 92 points this quarter.
			Revenue grew by 45% compared to last year.
			Market share is now at 23% in our primary market.
			Customer churn decreased to 5% from 8%.
			New user acquisition cost is $43 per user.
			Product adoption rate increased to 78%.
			Employee satisfaction is at 87 points.
			Operating margin improved to 34%.
			""";

	@Bean
	public CommandLineRunner commandLineRunner(ChatClient.Builder chatClientBuilder) {
		return args -> {
			new ChainWorkflow(chatClientBuilder.build()).chain(report);
		};
	}
}
