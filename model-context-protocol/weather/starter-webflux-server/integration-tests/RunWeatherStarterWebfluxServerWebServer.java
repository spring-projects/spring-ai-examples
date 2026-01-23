///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.zeroturnaround:zt-exec:1.12
//DEPS com.fasterxml.jackson.core:jackson-databind:2.17.1
//JAVA 17
//FILES ExampleInfo.json
//SOURCES ../../../../integration-testing/jbang-lib/IntegrationTestUtils.java
//SOURCES ../../../../integration-testing/jbang-lib/WebServerTestUtils.java
//SOURCES ../../../../integration-testing/jbang-lib/McpTestUtils.java

/*
 * Integration test for weather/starter-webflux-server (WebFlux reactive SSE transport)
 */
public class RunWeatherStarterWebfluxServerWebServer {
    public static void main(String... args) throws Exception {
        McpTestUtils.runMcpWeatherServerTest("weather-starter-webflux-server", 20, "http://localhost:8080/mcp");
    }
}
