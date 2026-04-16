# spring-ai-examples

[![Integration Tests](https://github.com/spring-projects/spring-ai-examples/actions/workflows/integration-tests.yml/badge.svg)](https://github.com/spring-projects/spring-ai-examples/actions/workflows/integration-tests.yml)

Practical Spring AI sample applications covering chat models, agentic workflows, MCP servers and clients, Kotlin examples, prompt engineering, and integration-testing support.

## Example Groups

| Area | Highlights | Path |
| --- | --- | --- |
| Advisors | Recursive advisors and tool argument augmentation demos | [`advisors/`](advisors/) |
| Agentic Patterns | Chain, routing, parallelization, orchestrator-workers, evaluator-optimizer | [`agentic-patterns/`](agentic-patterns/) |
| Agents | Reflection-style agent examples | [`agents/`](agents/) |
| Integration Testing | Test runner, scaffolding utilities, and testing docs for examples | [`integration-testing/`](integration-testing/) |
| Kotlin | Kotlin-first chat and RAG examples | [`kotlin/`](kotlin/) |
| Model Context Protocol | MCP servers, clients, sampling, SQLite, and web-search examples | [`model-context-protocol/`](model-context-protocol/) |
| Models | Core chat model examples | [`models/`](models/) |
| Prompt Engineering | Prompt engineering pattern examples | [`prompt-engineering/`](prompt-engineering/) |
| Misc | Additional experiments such as streaming responses and Claude skills demos | [`misc/`](misc/) |

## Getting Started

1. Pick an example group from the table above.
2. Open that group's README (if present) or a module-specific README for setup details.
3. Configure the provider credentials required by the selected example.
4. Build and run the example from its module directory.

## Configuration Notes

- Most examples expect credentials to be provided through environment variables.
- Check each module README for the exact provider variables required by that example.
