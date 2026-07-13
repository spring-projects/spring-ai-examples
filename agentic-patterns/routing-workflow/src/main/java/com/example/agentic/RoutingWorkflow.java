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

import java.util.Map;
import java.util.Objects;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.util.Assert;

/**
 * Routing workflow that uses an LLM to analyze input and select the most
 * appropriate route from a set of available options. The workflow focuses on
 * high-quality classification and returns the selected route key together with
 * model reasoning (captured internally as {@link RoutingResponse}).
 *
 * <p>
 * The routing workflow is particularly effective for complex tasks where:
 * <ul>
 * <li>There are distinct categories of input that are better handled
 * separately</li>
 * <li>Classification can be handled accurately by an LLM or traditional
 * classification model</li>
 * <li>Different types of input require different specialized processing or
 * expertise</li>
 * </ul>
 *
 * <p>
 * Key characteristics:
 * <ul>
 * <li>LLM-driven content analysis and classification</li>
 * <li>Clear separation of concerns: classification yields a route key that
 *     downstream components can act on</li>
 * <li>Extensible catalogue of routes defined by the caller</li>
 * </ul>
 * <p>
 * <p/>
 * The implementation uses the <a href=
 * "https://docs.spring.io/spring-ai/reference/1.0/api/structured-output-converter.html">Spring
 * AI Structured Output</a> feature to deserialize the model response into a
 * {@link RoutingResponse}.
 *
 * @author Christian Tzolov, Joonas Vali
 * @see ChatClient
 * @see <a href=
 * "https://docs.spring.io/spring-ai/reference/1.0/api/chatclient.html">Spring
 * AI ChatClient</a>
 * @see <a href=
 * "https://www.anthropic.com/research/building-effective-agents">Building
 * Effective Agents</a>
 * @see <a href=
 * "https://docs.spring.io/spring-ai/reference/1.0/api/structured-output-converter.html">Spring
 * AI Structured Output</a>
 */
public class RoutingWorkflow {

    private final ChatClient chatClient;

    public RoutingWorkflow(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Analyzes input with an LLM, evaluates the provided routes, and returns the
     * selected route key.
     *
     * <p>
     * The method:
     * <ol>
     * <li>Examines the input content and context</li>
     * <li>Considers the available route options and their descriptions</li>
     * <li>Generates reasoning and selects the best-matching route</li>
     * </ol>
     *
     * @param input  the input text to classify
     * @param routes map of route keys to human-readable descriptions
     * @return the selected route key
     */
    public String route(String input, Map<String, String> routes) {
        Assert.notNull(input, "Input text cannot be null");
        Assert.notEmpty(routes, "Routes map cannot be null or empty");

        // Determine the appropriate route for the input
        String routeKey = determineRoute(input, routes);

        if (!routes.containsKey(routeKey)) {
            // LLM failure handling, retry, etc..
            System.err.printf("Failed to detect the route, instead detected '%s', selecting fallback.%n", routeKey);
            // Alternatively have fallback defined as a parameter or return Optional value...
            return routes.keySet().stream().findFirst().orElseThrow();
        }

        return routeKey;
    }

    /**
     * Analyzes the input content and determines the most appropriate route based on
     * content classification. The classification process considers key terms,
     * context,
     * and patterns in the input to select the optimal route.
     *
     * <p>
     * The method uses an LLM to:
     * <ul>
     * <li>Analyze the input content and context</li>
     * <li>Consider the available routing options</li>
     * <li>Provide reasoning for the routing decision</li>
     * <li>Select the most appropriate route</li>
     * </ul>
     *
     * @param input           The input text to analyze for routing
     * @param availableRoutes The map of available routing options to their description
     * @return The selected route key based on content analysis
     */
    private String determineRoute(String input, Map<String, String> availableRoutes) {
        System.out.println("\nAvailable routes: " + availableRoutes.keySet());

        StringBuilder optionsWithDesc = new StringBuilder();
        availableRoutes.forEach((k, v) -> {
            optionsWithDesc.append(String.format("- %s: %s\n", k, v));
        });

        String selectorPrompt = String.format("""
            Analyze the input and select the most appropriate route from these options:
            
            %s
            
            First explain your reasoning, then provide your selection.
            
            Input: %s""", optionsWithDesc, input);

        RoutingResponse routingResponse = chatClient.prompt(selectorPrompt).call().entity(RoutingResponse.class);

        System.out.printf("Routing Analysis:%s\nSelected route: %s%n",
            Objects.requireNonNull(routingResponse).reasoning(), routingResponse.selection());

        return routingResponse.selection();
    }
}
