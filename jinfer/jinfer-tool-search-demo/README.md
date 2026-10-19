# Tool Search Tool Autoconfig with jinfer

Demonstrates the Spring AI [Tool Search Tool](https://docs.spring.io/spring-ai/reference/api/tools.html)
advisor on a fully local stack. The **chat model** and the **embedding model** both run inside the
application's JVM through [Qxotic jinfer](https://qxotic.ai). There are no API keys and no external services.

The application registers 26 tools: 3 `MyTools` and 23 `DummyTools`. The Spring AI
`ToolSearchToolCallingAdvisor` shows the model only `toolSearchTool`. When the model searches, the advisor
adds the matching tools for the next turn. The tool index is a `SimpleVectorStore`, and the jinfer
`EmbeddingModel` computes its embeddings.

```
USER   TOOLS: ["toolSearchTool"]
ASSIST TOOL-CALL: toolSearchTool ({"query":"get current time and date"})
USER   TOOLS: ["currentTime","weather","toolSearchTool"]
ASSIST TOOL-CALL: currentTime ({"location":"Landsmeer, NL"})
ASSIST TOOL-CALL: toolSearchTool ({"query":"clothing shops in Landsmeer, NL"})
USER   TOOLS: ["toolSearchTool","clothing"]
ASSIST TOOL-CALL: clothing ({"location":"Landsmeer, NL","openAtTime":"2026-10-19T16:06:43"})
ASSIST Based on the current time in Landsmeer, NL, here are the clothing shops that are open: Foo, Bar, Baz ...
```

## Run

Requirements: JDK 25 and Maven 3.9+. The example uses Spring Boot 4.1.1 and Spring AI 2.1.0-SNAPSHOT.

```bash
sdk use java 25.0.3-graal
mvn spring-boot:run
```

On the first start, two models are downloaded and cached in `~/Library/Caches/jinfer` (`~/.cache/jinfer`
on Linux):

| Role | Model | Size |
|------|-------|------|
| Chat | `unsloth/gemma-4-E2B-it-GGUF` (Q8_0) | 4.8 GB |
| Embeddings | `LiquidAI/LFM2.5-Embedding-350M-GGUF:Q8_0` | 362 MB |

You can override either model without editing files:

```bash
JINFER_CHAT_MODEL=LiquidAI/LFM2.5-8B-A1B-GGUF:Q8_0 \
JINFER_EMBEDDING_MODEL=Qwen/Qwen3-Embedding-0.6B-GGUF:Q8_0 mvn spring-boot:run
```

Model choice matters. Tool search needs a model that is willing to call a tool it has never seen
described in the prompt. `LFM2.5-1.2B-Instruct` replied "I don't have access to real-time data" and
never called `toolSearchTool`. `gemma-4-E2B-it` and `LFM2.5-8B-A1B` (MoE, ~1B active parameters, 8.5 GB)
both complete the full loop. Greedy decoding (`temperature=0.0`) keeps the multi-round tool loop
reproducible.

Switch the index with `spring.ai.chat.client.tool-search-advisor.tool-index-type`:

- `vector`: semantic search with the jinfer embeddings (default)
- `lucene`: keyword search
- `regex`: pattern matching
