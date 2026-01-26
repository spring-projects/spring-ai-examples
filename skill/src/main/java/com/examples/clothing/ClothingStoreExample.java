/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.examples.clothing;

import java.time.Duration;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

import com.examples.clothing.skills.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.skill.core.DefaultSkillKit;
import org.springframework.ai.skill.core.SkillBox;
import org.springframework.ai.skill.core.SkillKit;
import org.springframework.ai.skill.core.SkillPoolManager;
import org.springframework.ai.skill.spi.SkillAwareAdvisor;
import org.springframework.ai.skill.spi.SkillAwareToolCallingManager;
import org.springframework.ai.skill.support.DefaultSkillPoolManager;
import org.springframework.ai.skill.support.SimpleSkillBox;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Clothing Store Purchase Assistant Example
 *
 * <p>This example demonstrates how to build an intelligent clothing store purchase assistant
 * using the SpringAI Skill Framework. Through AI conversations, store owners can receive
 * purchase recommendations based on multiple factors:
 *
 * <ul>
 *   <li>📦 Inventory Management - Check current stock status
 *   <li>💰 Pricing Analysis - Analyze store pricing and profit margins
 *   <li>🏭 Supplier Catalog - Query available items and wholesale costs
 *   <li>📈 Sales Trends - Understand hot-selling items and market demand
 *   <li>🌤️ Weather Query - Consider weather impact on clothing sales
 *   <li>👗 Fashion Guide - Get fashion trends and reference materials
 *   <li>🎯 Purchase Strategy - Receive intelligent restocking recommendations
 * </ul>
 *
 * <p><b>How to Run</b>:
 *
 * <pre>
 * mvn exec:java -pl spring-ai-skill-extension-examples \
 *   -Dexec.mainClass="com.semir.spring.ai.skill.examples.clothing.ClothingStoreExample"
 * </pre>
 *
 * <p><b>Example Conversations</b>:
 *
 * <pre>
 * User: "Show me current inventory status"
 * AI: [Calls checkInventory tool and shows inventory report]
 *
 * User: "What's the sales trend this week?"
 * AI: [Calls getSalesTrends tool and analyzes hot-selling items]
 *
 * User: "I have a $10,000 budget, what should I buy?"
 * AI: [Calls generatePurchaseStrategy and provides optimized recommendations]
 * </pre>
 *
 * @author Semir
 */
public class ClothingStoreExample {

    private static final Logger logger = LoggerFactory.getLogger(ClothingStoreExample.class);

    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("🏪 Clothing Store Purchase Assistant");
        System.out.println("=".repeat(80));
        System.out.println();

        try {
            // 1. Initialize framework components
            SkillPoolManager poolManager = new DefaultSkillPoolManager();
            SimpleSkillBox skillBox = new SimpleSkillBox();
            SkillKit skillKit = DefaultSkillKit.builder()
                    .skillBox(skillBox)
                    .poolManager(poolManager)
                    .build();

            // 2. Register all skills
            System.out.println("📦 Registering skills...\n");

            skillBox.addSource("example");

            // Instance-based registration (eager loading)
            skillKit.register(InventorySkill.create());
            System.out.println("  ✓ Inventory Management Skill registered");

            skillKit.register(PricingSkill.create());
            System.out.println("  ✓ Pricing Analysis Skill registered");

            skillKit.register(TrendSkill.create());
            System.out.println("  ✓ Sales Trend Skill registered");

            // Class-based registration (lazy loading)
            skillKit.register(SupplierSkill.class);
            System.out.println("  ✓ Supplier Catalog Skill registered (lazy)");

            skillKit.register(PurchaseStrategySkill.class);
            System.out.println("  ✓ Purchase Strategy Skill registered (lazy)");

            // Weather Skill (weather impacts clothing sales)
            skillKit.register(WeatherSkill.class);
            System.out.println("  ✓ Weather Skill registered (lazy)");

            // Fashion Guide Skill (provides fashion trends and reference materials)
            skillKit.register(FashionGuideSkill.class);
            System.out.println("  ✓ Fashion Guide Skill registered (lazy)");

            System.out.println("\n✅ All skills registered and automatically added to SkillBox!\n");

            // 4. Create AI Chat Client
            System.out.println("🤖 Initializing AI Assistant...\n");

            SkillAwareToolCallingManager toolManager =
                    SkillAwareToolCallingManager.builder().skillKit(skillKit).build();
            ChatClient chatClient = createChatClient(toolManager, skillKit);

            System.out.println("✅ AI Assistant ready!\n");
            System.out.println("=".repeat(80));
            System.out.println();

            // 5. Start interactive chat
            interactiveChat(chatClient, skillBox, poolManager);

        } catch (Exception e) {
            logger.error("Error running clothing store example", e);
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static ChatClient createChatClient(SkillAwareToolCallingManager toolManager, SkillKit skillKit) {

        String baseUrl = System.getenv("BASE_URL");
        String apiKey = System.getenv("API_KEY");
        String completionsPath = System.getenv("COMPLETIONS_PATH");
        String model = System.getenv("MODEL");

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(Duration.ofSeconds(120));

        RestClient.Builder builder = RestClient.builder().requestFactory(requestFactory);

        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .completionsPath(completionsPath)
                .restClientBuilder(builder)
                .build();

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(model)
                .maxTokens(16000)
                .temperature(0.8)
                .build();

        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options)
                .toolCallingManager(toolManager)
                .build();

        // Define system prompt
        String systemPrompt =
                """
                You are an expert clothing store purchase assistant helping store owners make smart inventory decisions.

                Your role:
                - Analyze inventory status and identify restocking needs
                - Consider sales trends and seasonal factors
                - Provide data-driven purchase recommendations
                - Help optimize budget allocation
                - Calculate ROI and profit projections
                - Consider weather when suggesting seasonal items
                - Provide fashion trend insights and reference materials

                Guidelines:
                - Always check inventory before making recommendations
                - Prioritize items with stock-out risk
                - Consider profit margins and sales velocity
                - Recommend bulk purchases when beneficial
                - When asked about fashion references, use loadSkillReference tool
                - Be concise but thorough in your analysis
                - Use data from tools rather than assumptions

                When answering, always use the skills provided. Respond professionally and amicably, like a business advisor.
                """;

        // Create ChatClient
        return ChatClient.builder(openAiChatModel)
                .defaultSystem(systemPrompt)
                .defaultAdvisors(SkillAwareAdvisor.builder().skillKit(skillKit).build())
                .build();
    }

    private static void interactiveChat(ChatClient chatClient, SkillBox skillBox, SkillPoolManager skillPoolManager) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("💬 Chat with the AI Assistant (type 'exit' or 'quit' to end)\n");
        System.out.println("Example questions:");
        System.out.println("  • \"Show me current inventory status\"");
        System.out.println("  • \"Check the weather in New York\"");
        System.out.println("  • \"Show me the spring trends report\" ← Triggers reference loading!");
        System.out.println("  • \"Give me the buying guide link\" ← Triggers reference loading!");
        System.out.println("  • \"Generate a purchase strategy with $10,000 budget\"");
        System.out.println();
        System.out.println("-".repeat(80));
        System.out.println();

        while (true) {
            // User input
            System.out.print("🧑 You: ");
            String userInput = scanner.nextLine().trim();

            if (userInput.isEmpty()) {
                continue;
            }

            // Exit command
            if (userInput.equalsIgnoreCase("exit") || userInput.equalsIgnoreCase("quit")) {
                System.out.println("\n👋 Thank you for using Clothing Store Assistant. Goodbye!");
                break;
            }

            // Special command: view statistics
            if (userInput.equalsIgnoreCase("/stats")) {
                printStats(skillBox, skillPoolManager);
                continue;
            }

            try {
                System.out.print("\n🤖 Assistant: ");
                AtomicReference<String> lastContent = new AtomicReference<>("");

                // Stream response
                chatClient.prompt().user(userInput).stream()
                        .chatResponse()
                        .doOnNext(chatResponse -> {
                            if (chatResponse.getResult() != null
                                    && chatResponse.getResult().getOutput() != null) {
                                String currentContent =
                                        chatResponse.getResult().getOutput().getText();
                                if (currentContent != null) {
                                    String lastContentValue = lastContent.get();
                                    if (currentContent.startsWith(lastContentValue)) {
                                        // Incremental output
                                        String newContent = currentContent.substring(lastContentValue.length());
                                        System.out.print(newContent);
                                        System.out.flush();
                                        lastContent.set(currentContent);
                                    } else {
                                        // Full output
                                        System.out.print(currentContent);
                                        System.out.flush();
                                        lastContent.set(currentContent);
                                    }
                                }
                            }
                        })
                        .blockLast();

                System.out.println("\n");
                System.out.println("-".repeat(80));

            } catch (Exception e) {
                System.err.println("\n❌ Error occurred: " + e.getMessage());
                logger.error("Error during chat interaction", e);
            }
        }

        scanner.close();
    }

    private static void printStats(SkillBox skillBox, SkillPoolManager skillPoolManager) {
        System.out.println("\n📊 Framework Statistics:");
        System.out.println("  • Total registered skills: 7");
        System.out.println("  • Skills in box: " + skillBox.getSkillCount());
        System.out.println("  • Registered definitions: "
                + skillPoolManager.getDefinitions().size());
        System.out.println("  • Categories: Inventory, Pricing, Supplier, Trend, Weather, Fashion, Purchase");
        System.out.println();
    }
}
