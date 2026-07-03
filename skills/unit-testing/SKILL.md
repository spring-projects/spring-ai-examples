---
name: unit-testing
description: Write unit and integration tests for the spring-ai-examples project using JUnit 5 and Spring Boot test conventions. Use when creating new test files, adding test coverage, or fixing failing tests.
---

## Test writing guidelines

### Test framework

- **JUnit 5 (Jupiter)** — always use `org.junit.jupiter.api.Test`.
- **Spring Boot test** — use `org.springframework.boot.test.context.SpringBootTest` for integration tests.
- **Assertions** — use `org.junit.jupiter.api.Assertions.*` with static imports.
- Use `@DisplayName` to describe test intent in plain language.

### Test class location

- Place tests in `src/test/java/` (Java) or `src/test/kotlin/` (Kotlin) under the same package as the source class.
- Test class name: `<ClassName>Tests` (e.g., `WeatherServiceTests`).

### Structure (AAA pattern)

Organize each test method with clear Arrange-Act-Assert sections:

```java
@Test
@DisplayName("should return forecast when location is valid")
void getWeatherForecastByLocation_ValidLocation_ReturnsForecast() {
    // Arrange
    double latitude = 47.6062;
    double longitude = -122.3321;

    // Act
    String result = weatherService.getWeatherForecastByLocation(latitude, longitude);

    // Assert
    assertNotNull(result);
    assertTrue(result.contains("Temperature"));
}
```

### Naming convention

Use `methodName_StateUnderTest_ExpectedBehavior`:

| Pattern | Example |
|---|---|
| `method_condition_result` | `getForecast_validLocation_returnsForecast()` |
| `method_nullInput_throwsException` | `route_nullInput_throwsIllegalArgumentException()` |
| `method_edgeCase_handlesGracefully` | `getAlerts_emptyState_returnsEmpty()` |

### What to test in this project

| Module | Testing focus |
|---|---|
| Agent workflows | Verify routing logic, prompt construction, response parsing |
| Weather services | Test API client calls (mock RestClient), JSON deserialization, error handling |
| Function callbacks | Test `Function<WeatherRequest, WeatherResponse>` implementations with known inputs |
| MCP servers | Test tool registration, response format, and error scenarios |

### Mocking

- Use **Mockito** for mocking dependencies (included via `spring-boot-starter-test`).
- Annotate mocks with `@Mock` / `@InjectMocks` or use `Mockito.mock()`.
- Verify interactions with `Mockito.verify()` when behavior must be confirmed.

```java
@ExtendWith(MockitoExtension.class)
class WeatherServiceTests {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @InjectMocks
    private WeatherService weatherService;

    @Test
    @DisplayName("should throw exception when API returns null points")
    void getWeatherForecastByLocation_nullPoints_throwsException() {
        // Arrange
        given(restClient.get()).willReturn(requestBodyUriSpec);
        // ... mock chain

        // Act & Assert
        assertThrows(Exception.class,
            () -> weatherService.getWeatherForecastByLocation(0, 0));
    }
}
```

### Integration tests

Use `@SpringBootTest` for full context loading. Only use when testing Spring wiring or end-to-end behavior:

```java
@SpringBootTest
class RoutingWorkflowTests {

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Test
    @DisplayName("application context loads successfully")
    void contextLoads() {
    }
}
```

### What to avoid

- Do not test Spring internals — focus on your application logic.
- Do not use `@SpringBootTest` when a plain JUnit 5 + Mockito test suffices (slow).
- Do not write tests that depend on external APIs — mock HTTP calls.
- Do not leave `System.out.println` in test code — use assertions.
- Do not use `Thread.sleep()` for async coordination — use `awaitility` or `CompletableFuture`.

### Checklist

Before finalizing a test:

- [ ] Test class name ends with `Tests`
- [ ] Uses `@Test` and `@DisplayName`
- [ ] Follows AAA pattern with clear sections
- [ ] Method name follows `methodName_condition_result` convention
- [ ] No mutable shared state between tests
- [ ] Mocks external dependencies where appropriate
- [ ] Covers: happy path, error cases, edge cases (null, empty, boundary)
