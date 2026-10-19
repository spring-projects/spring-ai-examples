# jinfer Spring AI demo

A Spring Boot + Spring AI application that runs a local LLM **inside its own JVM** with
[Qxotic jinfer](https://qxotic.ai). There is no Ollama, no Docker, no Python and no API key. The model is a
GGUF file that is downloaded from Hugging Face on first start and cached.

The application code is plain Spring AI (`ChatClient`, `@Tool`, `.entity(...)`, session memory). jinfer only
appears as the provider in the POM and in `application.properties`.

## Endpoints

| Endpoint | What it shows |
|----------|---------------|
| `GET /chat?message=...` | Blocking chat call |
| `GET /chat/stream?message=...` | Token streaming (SSE) |
| `GET /chat/memory?message=...&conversationId=...` | Multi-turn chat with [session memory](https://docs.spring.io/spring-ai/reference/api/session.html) (`SessionAdvisors`) |
| `GET /weather?city=...` | Tool calling (`@Tool` in `WeatherTools`) |
| `POST /incident` (text body) | Structured output to a Java record, with sampling constrained by a grammar |

## Run it

Requirements: **JDK 25** (jinfer uses the FFM and Vector APIs) and Maven 3.9+.

This example uses Spring Boot 4.1.1 and Spring AI 2.1.0-SNAPSHOT.

```bash
sdk use java 25.0.3-graal          # or any JDK 25; check with: java -version
mvn spring-boot:run
```

The first start downloads `LiquidAI/LFM2.5-1.2B-Instruct-GGUF:Q8_0` (about 1.2 GB) into
`~/Library/Caches/jinfer` on macOS or `~/.cache/jinfer` on Linux. Later starts take a few seconds.

```bash
curl "localhost:8080/chat?message=What%20is%20the%20capital%20of%20Bulgaria%3F"
curl -N "localhost:8080/chat/stream?message=Write%20a%20haiku%20about%20rivers"
curl "localhost:8080/weather?city=Sofia"
curl -X POST -H 'Content-Type: text/plain' \
     --data 'Checkout has returned HTTP 503 for ten minutes. Wake someone up!' localhost:8080/incident
curl "localhost:8080/chat/memory?conversationId=c1&message=Hi,%20my%20name%20is%20Christian."
curl "localhost:8080/chat/memory?conversationId=c1&message=What%20is%20my%20name%3F"
```

To use a different model, change `spring.ai.jinfer.chat.model` or set an environment variable:

```bash
JINFER_CHAT_MODEL=unsloth/Qwen3.5-2B-GGUF:Q8_0 mvn spring-boot:run
```

To run the jar directly, pass the JVM flags yourself:

```bash
mvn package
java --add-modules jdk.incubator.vector --enable-native-access=ALL-UNNAMED \
     -jar target/jinfer-spring-ai-demo-0.0.1-SNAPSHOT.jar
```

---

## Bootstrapping your own jinfer + Spring AI project

**The artifacts are on Maven Central** under the group `com.qxotic`. The current version is `0.3.1`
(for example, [jinfer-bom](https://repo1.maven.org/maven2/com/qxotic/jinfer-bom/)). You do not need to
build the `qxotic` repository or add an extra Maven repository.

### 1. Generate a Spring Boot project

Use [start.spring.io](https://start.spring.io) (or `spring init`) with:

- **Spring Boot 4.1.x** and **Java 25**
- the **Spring Web** dependency, if you want REST endpoints
- **no** Spring AI model starter. jinfer is not listed on start.spring.io, so you add it by hand.

jinfer 0.3.1 is built and tested against Spring Boot 4.1.1 and Spring AI 2.0.1, and also runs on Spring AI 2.1.0-SNAPSHOT, which this example uses.

### 2. Import both BOMs

```xml
<properties>
  <java.version>25</java.version>
</properties>

<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.springframework.ai</groupId>
      <artifactId>spring-ai-bom</artifactId>
      <version>2.0.1</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
    <dependency>
      <groupId>com.qxotic</groupId>
      <artifactId>jinfer-bom</artifactId>
      <version>0.3.1</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

### 3. Add the starter, at least one model family, and optional fast kernels

```xml
<dependency>
  <groupId>com.qxotic</groupId>
  <artifactId>jinfer-spring-ai-spring-boot-starter</artifactId>
</dependency>
<!-- architecture code only, no weights; or pick one: jinfer-lfm2, jinfer-qwen35, jinfer-gemma4, jinfer-llama, jinfer-gptoss ... -->
<dependency>
  <groupId>com.qxotic</groupId>
  <artifactId>jinfer-models-all</artifactId>
</dependency>
<!-- optional matmul acceleration: native SIMD, with a pure-Java Vector API fallback -->
<dependency>
  <groupId>com.qxotic</groupId>
  <artifactId>jam-native</artifactId>
  <scope>runtime</scope>
</dependency>
<dependency>
  <groupId>com.qxotic</groupId>
  <artifactId>jam-vector</artifactId>
  <scope>runtime</scope>
</dependency>
```

The starter brings `spring-ai-client-chat`, `spring-ai-rag`, and the auto-configuration for
`ChatClient.Builder`, tool calling and `ChatMemory`.

### 4. Pass the required JVM flags

jinfer needs `--add-modules jdk.incubator.vector --enable-native-access=ALL-UNNAMED`:

```xml
<plugin>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-maven-plugin</artifactId>
  <configuration>
    <optimizedLaunch>false</optimizedLaunch>   <!-- keep the C2 JIT; it matters for inference speed -->
    <jvmArguments>--add-modules jdk.incubator.vector --enable-native-access=ALL-UNNAMED</jvmArguments>
  </configuration>
</plugin>
```

Pass the same flags to `maven-surefire-plugin` (`<argLine>`) for tests, to `java -jar`, and to your IDE
run configuration.

### 5. Configure the model

```properties
spring.ai.model.chat=jinfer
spring.ai.jinfer.chat.model=LiquidAI/LFM2.5-1.2B-Instruct-GGUF:Q8_0   # a HF ref or a local /path/model.gguf
spring.ai.jinfer.chat.context-capacity=8192
spring.ai.jinfer.chat.max-tokens=512
```

Then inject `ChatClient.Builder` (or `ChatModel`) and write normal Spring AI code.

Other `spring.ai.jinfer.*` capabilities work the same way:

| Capability | Selection | Model property |
|------------|-----------|----------------|
| Chat | `spring.ai.model.chat=jinfer` | `spring.ai.jinfer.chat.model` |
| Embeddings | `spring.ai.model.embedding=jinfer` | `spring.ai.jinfer.embedding.model` (e.g. `Qwen/Qwen3-Embedding-0.6B-GGUF:Q8_0`) |
| Reranking | present when configured | `spring.ai.jinfer.rerank.model` |
| Text-to-speech | present when configured | `spring.ai.jinfer.speech.model` |
| Transcription | present when configured | `spring.ai.jinfer.transcription.model` |

Multimodal models attach a projector as a companion, for example
`spring.ai.jinfer.chat.companions.media=unsloth/gemma-4-E2B-it-GGUF/mmproj-F32.gguf`.

### Useful environment variables

- `JINFER_MODELS`: overrides the model cache directory
- `JINFER_OFFLINE=1`: uses cached models only and never downloads

### Further reading in the `qxotic` repository

- `qxotic/jinfer/jinfer-spring-ai/README.md`: the full Spring AI integration guide (vision, embeddings,
  reranking, prompt caching, TTS, transcription)
- `qxotic/jinfer/jinfer-example-local-rag`: a complete in-JVM RAG application
- `qxotic/jinfer/jinfer-example-judge-advisor`: a local grammar-constrained judge in a self-refine advisor
