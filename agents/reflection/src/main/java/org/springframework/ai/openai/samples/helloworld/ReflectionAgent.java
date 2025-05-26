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
package org.springframework.ai.openai.samples.helloworld;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

@Component
public class ReflectionAgent {

    private final ChatClient generateChatClient;

    private final ChatClient critiqueChatClient;


    public ReflectionAgent(ChatModel chatModel) {
        this.generateChatClient = ChatClient.builder(chatModel)
                .defaultSystem("""
                        You are a Java programmer tasked with generating high quality Java code.
                        Your task is to Generate the best content possible for the user's request. If the user provides critique,
                        respond with a revised version of your previous attempt.
                        """)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(MessageWindowChatMemory.builder().build()).build())
                .build();

        this.critiqueChatClient = ChatClient.builder(chatModel)
                .defaultSystem("""
                        You are tasked with generating critique and recommendations to the user's generated content.
                        If the user content has something wrong or something to be improved, output a list of recommendations
                        and critiques. If the user content is ok and there's nothing to change, output this: <OK>
                        """)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(MessageWindowChatMemory.builder().build()).build())
                .build();
    }

    public String run(String userQuestion, int maxIterations) {

        String generation = generateChatClient.prompt(userQuestion).call().content();
        System.out.println("## Generation\n\n" + generation);
        String critique;
        for (int i = 0; i < maxIterations; i++) {

            critique = critiqueChatClient.prompt(generation).call().content();

            System.out.println("## Critique\n\n" + critique);
            if (critique.contains("<OK>")) {
                System.out.println("\n\nStop sequence found\n\n");
                break;
            }
            generation = generateChatClient.prompt(critique).call().content();
        }
        return generation;

    }

}
