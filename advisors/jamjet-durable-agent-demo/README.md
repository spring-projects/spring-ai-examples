# JamJet Durable Agent Demo

Demonstrates how to add **durable execution** to a Spring AI agent with zero code changes using [JamJet](https://github.com/jamjet-labs/jamjet-spring).

## What you get

By adding `jamjet-spring-boot-starter` to your dependencies, every `ChatClient.call()` automatically gains:

- **Crash recovery** — if the app crashes mid-call, execution resumes from the last checkpoint
- **Event-sourced audit trails** — every prompt, response, and tool call is logged
- **Execution tracking** — workflow state visible via the JamJet runtime API

No code changes needed. JamJet integrates via Spring AI's `BaseAdvisor` + `ChatClientCustomizer` pattern.

## Prerequisites

- Java 21+
- Docker (for JamJet runtime)
- OpenAI API key

## Running

```bash
# 1. Start JamJet runtime
docker run -p 7700:7700 ghcr.io/jamjet-labs/jamjet:latest

# 2. Run the demo
OPENAI_API_KEY=sk-... mvn spring-boot:run
```

## What happens

1. The app creates a `ChatClient` with two tools (`getWeather`, `getClothingAdvice`)
2. JamJet's `DurabilityAdvisor` and `AuditAdvisor` are auto-injected — no manual wiring needed
3. When the agent runs, JamJet:
   - Creates a workflow on the runtime
   - Starts a tracked execution
   - Records every tool call and LLM response as immutable events
   - If the process crashes, the next run resumes from the last checkpoint

## Configuration

```properties
spring.jamjet.runtime-url=http://localhost:7700  # JamJet runtime address
spring.jamjet.audit.enabled=true                 # Audit trails (default: on)
spring.jamjet.audit.include-prompts=true          # Log prompt text
spring.jamjet.audit.include-responses=true        # Log response text
```

## Learn more

- [JamJet Spring Boot Starter](https://github.com/jamjet-labs/jamjet-spring) — full documentation
- [JamJet Docs](https://docs.jamjet.dev) — runtime setup, configuration, API reference
- [Maven Central](https://central.sonatype.com/artifact/dev.jamjet/jamjet-spring-boot-starter) — published artifacts
