# Building and deploying examples

Build and run the tests for an individual Maven example:

```bash
./scripts/build-example.sh agentic-patterns/chain-workflow
```

Additional Maven arguments can be passed after the module path, for example:

```bash
./scripts/build-example.sh agentic-patterns/chain-workflow -DskipTests
```

The build script requires Java 25 or newer. It uses `JAVA_HOME` when set;
otherwise, it looks for Java 25 in the macOS JDK registry or under `~/.jdk`.

Build a Java 25 Docker image and run a selected Spring Boot example in the
background. Choose from an interactive menu:

```bash
./scripts/deploy-example-docker.sh
# Or explicitly request the menu:
./scripts/deploy-example-docker.sh --select
```

Alternatively, pass the module path directly:

```bash
./scripts/deploy-example-docker.sh agentic-patterns/chain-workflow \
  --image chain-workflow:latest \
  --name chain-workflow \
  --port 8080:8080 \
  --env-file .env
```

For the local Ollama advisor example, pass the host endpoint and installed
model. No Anthropic API key is needed:

```bash
./scripts/deploy-example-docker.sh advisors/evaluation-recursive-advisor-demo \
  --ollama-model qwen3.5:latest \
  --port none
```

The Ollama URL defaults to `http://host.docker.internal:11434`, which reaches
the host from Docker Desktop containers. Override it with `--ollama-url` if
your server uses another address. `--ollama-model` selects the model used for
both responses and evaluation. The Ollama server must be reachable from Docker
and the selected model must support tool calling.
For services that actually use an Anthropic-compatible API, `--anthropic-url`
and `--anthropic-api-key` remain available; avoid putting real keys directly
on the command line because they may appear in shell history and process
metadata.

The image and container names default to `spring-ai-<module-path>` and the
published port defaults to `8080:8080`. Use `--port none` for applications
that do not expose an HTTP port. Supply an environment file for required
application configuration such as model API keys. The container uses the
Java 25 Temurin JRE image and is configured to restart unless stopped.

Stop and remove the container with:

```bash
docker rm -f chain-workflow
```
