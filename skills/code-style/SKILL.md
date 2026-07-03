---
name: code-style
description: Enforce Java/Kotlin code style conventions for the spring-ai-examples project. Use when writing new code, reviewing pull requests, or fixing formatting issues.
---

## Code style rules for spring-ai-examples

### Indentation & formatting

- Use **tabs** for indentation (not spaces), matching Spring Framework conventions.
- Egyptian (1TBS) brace style: opening brace on the same line.
- One blank line between methods.
- No trailing whitespace.

### Naming conventions

| Element | Convention | Example |
|---|---|---|
| Classes | PascalCase | `RoutingWorkflow` |
| Methods | camelCase | `getWeatherForecastByLocation` |
| Fields | camelCase | `chatClient` |
| Constants | UPPER_SNAKE_CASE | `BASE_URL` |
| Packages | lowercase | `com.example.agentic` |
| Records | PascalCase | `RoutingResponse` |

### Java language features

- Use `var` for local variable type inference where the type is obvious from context.
- Use Java records for DTOs and data carriers.
- Use text blocks (`""" ... """`) for multi-line strings and AI prompts.
- Use constructor injection for Spring beans. Avoid field injection with `@Autowired`.

### License header

Every source file must include the Apache License 2.0 header:

```java
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
```

### Spring conventions

- Constructor injection: declare `private final` fields and initialize via constructor parameters.
- Use `@Service`, `@Component`, `@Configuration` stereotype annotations appropriately.
- Use `@Bean` in `@Configuration` classes for factory methods.
- Use `@Tool(description = "...")` for Spring AI tool methods.
- Use `@JsonIgnoreProperties(ignoreUnknown = true)` on response records/classes.
- Use `@JsonProperty("name")` to map JSON fields.

### What to avoid

- No wildcard imports (`import java.util.*`).
- No `System.out.println` in production code — use a logger (SLF4J).
- No empty catch blocks — either log the exception or rethrow.
- No resource leaks — always use try-with-resources for `Closeable` types.
- No mutable static fields (thread safety).
- No raw `Map`/`List` types — always parameterize generics.

### Gotchas

- The root `pom.xml` is a thin aggregator POM. All modules inherit from `spring-boot-starter-parent`. Do not add plugins or dependency management to the root POM.
- Java 17 is the project target across all modules.
- Some modules use `@SpringBootTest` with `SpringApplication.run()` in their `Application.java`. For unit tests, prefer slicing with `@WebMvcTest` or plain JUnit 5.

### Verification

Before completing a code change:

- [ ] Indentation uses tabs consistently
- [ ] License header is present
- [ ] No unused imports
- [ ] No `System.out.println` (use `Logger`)
- [ ] No empty catch blocks
- [ ] All resources use try-with-resources
- [ ] No raw generic types
