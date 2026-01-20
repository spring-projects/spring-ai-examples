# Spring AI Skill Extension Example

This example demonstrates the **Spring AI Skill Extension Framework** - a powerful skill management framework for Spring AI applications that enables progressive skill loading, lazy instantiation, and dynamic tool management.

## Overview

The **Clothing Store Purchase Assistant** example showcases an intelligent purchase assistant that helps store owners make smart inventory decisions through AI conversations.

### Key Features

- 📦 **Inventory Management** - Check current stock status
- 💰 **Pricing Analysis** - Analyze store pricing and profit margins
- 🏭 **Supplier Catalog** - Query available items and wholesale costs
- 📈 **Sales Trends** - Understand hot-selling items and market demand
- 🌤️ **Weather Query** - Consider weather impact on clothing sales
- 👗 **Fashion Guide** - Get fashion trends and reference materials with progressive loading
- 🎯 **Purchase Strategy** - Receive intelligent restocking recommendations

### Framework Highlights

This example demonstrates:
- **Instance-based Registration** (eager loading) - Skills loaded at startup
- **Class-based Registration** (lazy loading) - Skills loaded on-demand
- **Progressive Content Loading** - Large skill content loaded only when needed
- **Skill References** - External reference materials loaded dynamically

## Prerequisites

1. **Java 17+**
2. **Maven 3.6+**
3. **Spring AI Skill Extension Core** - Version 1.1.0 (included as dependency)

## Configuration

Set the following environment variables before running:

```bash
export BASE_URL="https://api.openai.com"          # OpenAI API base URL
export API_KEY="your-api-key"                      # Your OpenAI API key
export COMPLETIONS_PATH="/v1/chat/completions"    # Completions endpoint path
export MODEL="gpt-4o"                              # Model to use
```

Or on Windows:

```cmd
set BASE_URL=https://api.openai.com
set API_KEY=your-api-key
set COMPLETIONS_PATH=/v1/chat/completions
set MODEL=gpt-4o
```

## Running the Example

### Option 1: Using Maven Exec Plugin

```bash
mvn exec:java -Dexec.mainClass="com.examples.clothing.ClothingStoreExample"
```

### Option 2: Run as Spring Boot Application

```bash
mvn spring-boot:run
```

### Option 3: Build and Run JAR

```bash
mvn clean package
java -jar target/spring-ai-skill-example-0.0.1-SNAPSHOT.jar
```

## Example Conversations

Once the assistant starts, try these queries:

```
🧑 You: Show me current inventory status
🤖 Assistant: [Calls checkInventory tool and shows inventory report]

🧑 You: What's the sales trend this week?
🤖 Assistant: [Calls getSalesTrends tool and analyzes hot-selling items]

🧑 You: Show me the spring trends report
🤖 Assistant: [Triggers progressive loading - loads FashionGuideSkill and its content]

🧑 You: I have a $10,000 budget, what should I buy?
🤖 Assistant: [Calls generatePurchaseStrategy and provides optimized recommendations]

🧑 You: Check the weather in New York
🤖 Assistant: [Calls WeatherSkill to get weather information]
```

### Special Commands

- `/stats` - View framework statistics
- `exit` or `quit` - End the conversation

## Architecture

This example uses the Spring AI Skill Extension Framework which provides:

### 1. Skill Registration

```java
// Instance-based (eager loading)
skillKit.register(InventorySkill.create());

// Class-based (lazy loading)
skillKit.register(WeatherSkill.class);
```

### 2. Dynamic Tool Management

The framework automatically:
- Converts skills to tool callbacks
- Manages skill lifecycle
- Handles lazy instantiation
- Enables progressive content loading

### 3. Integration with Spring AI

```java
SkillAwareToolCallingManager toolManager =
    SkillAwareToolCallingManager.builder()
        .skillKit(skillKit)
        .build();

ChatClient chatClient = ChatClient.builder(chatModel)
    .defaultAdvisors(SkillAwareAdvisor.builder()
        .skillKit(skillKit)
        .build())
    .build();
```

## Project Structure

```
skill/
├── src/main/java/com/semir/spring/ai/skill/examples/clothing/
│   ├── ClothingStoreExample.java          # Main application entry
│   └── skills/
│       ├── InventorySkill.java            # Inventory management (eager)
│       ├── PricingSkill.java              # Pricing analysis (eager)
│       ├── TrendSkill.java                # Sales trends (eager)
│       ├── SupplierSkill.java             # Supplier catalog (lazy)
│       ├── PurchaseStrategySkill.java     # Purchase strategy (lazy)
│       ├── WeatherSkill.java              # Weather info (lazy)
│       └── FashionGuideSkill.java         # Fashion guide (lazy + progressive)
├── src/main/resources/skills/
│   └── fashion-guide/                     # Progressive loading content
│       ├── content.md
│       └── references.json
└── pom.xml
```

## Dependencies

- **Spring Boot** 4.0.0
- **Spring AI** 1.1.0
- **Spring AI Skill Extension Core** 1.1.0
- **Spring AI OpenAI** (from Spring AI BOM)

## Learn More

- [Spring AI Skill Extension Framework](https://github.com/your-repo/spring-ai-skill-extension)
- [Spring AI Documentation](https://docs.spring.io/spring-ai/)
- [Example Source Code](src/main/java/com/examples/clothing/)

## License

Apache License 2.0
