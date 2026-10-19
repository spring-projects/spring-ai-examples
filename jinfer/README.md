# Spring AI with jinfer (local, in-JVM models)

These examples run open models **inside the application's JVM** with
[Qxotic jinfer](https://qxotic.ai), a Spring AI provider for GGUF models. There is no Ollama, no Docker,
no Python and no API key. Models are downloaded from Hugging Face on first use and cached.

The application code is plain Spring AI. jinfer appears only as the provider in the POM and in
`application.properties`.

| Example | What it shows |
|---------|---------------|
| [jinfer-spring-ai-demo](jinfer-spring-ai-demo) | Chat, streaming, tool calling, structured output and session memory over REST |
| [jinfer-tool-search-demo](jinfer-tool-search-demo) | The Tool Search Tool advisor with a local chat model and a local embedding model |
| [jinfer-transcription-demo](jinfer-transcription-demo) | Speech-to-text with NVIDIA Parakeet, with a browser page for recording or uploading audio |

## Requirements

- **JDK 25**: jinfer uses the FFM and Vector APIs.
- Maven 3.9+
- Spring Boot 4.1.1, Spring AI 2.1.0-SNAPSHOT and jinfer 0.3.1, all resolved by the POMs.

Every JVM that runs jinfer needs these flags:

```
--add-modules jdk.incubator.vector --enable-native-access=ALL-UNNAMED
```

The POMs pass them to `mvn spring-boot:run`. Pass them yourself to `java -jar` and to IDE run
configurations. In VS Code, add them to `.vscode/settings.json` so every Run/Debug launch gets them:

```json
"java.debug.settings.vmArgs": "--add-modules jdk.incubator.vector --enable-native-access=ALL-UNNAMED"
```

Without them, the jinfer model beans fail to start.

## Build

```bash
sdk use java 25.0.3-graal   # or any JDK 25
cd jinfer
./mvnw clean package
```

The repository's root build includes this folder only when it runs on JDK 25 or newer (the `java25`
profile).

## Model cache

Model references such as `LiquidAI/LFM2.5-1.2B-Instruct-GGUF:Q8_0` are downloaded once and cached in
`~/Library/Caches/jinfer` on macOS or `~/.cache/jinfer` on Linux. Set `JINFER_MODELS` to use another
directory, or `JINFER_OFFLINE=1` to use cached models only.
