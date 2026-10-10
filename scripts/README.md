# Building and deploying examples

Build and run the tests for an individual Maven example:

```bash
./scripts/build-example.sh agentic-patterns/chain-workflow
```

Additional Maven arguments can be passed after the module path, for example:

```bash
./scripts/build-example.sh agentic-patterns/chain-workflow -DskipTests
```

Build a Java 25 Docker image and run the selected Spring Boot example in the
background:

```bash
./scripts/deploy-example-docker.sh agentic-patterns/chain-workflow \
  --image chain-workflow:latest \
  --name chain-workflow \
  --port 8080:8080 \
  --env-file .env
```

The image and container names default to `spring-ai-<module-path>` and the
published port defaults to `8080:8080`. Use `--port none` for applications
that do not expose an HTTP port. Supply an environment file for required
application configuration such as model API keys. The container uses the
Java 25 Temurin JRE image and is configured to restart unless stopped.

Stop and remove the container with:

```bash
docker rm -f chain-workflow
```
