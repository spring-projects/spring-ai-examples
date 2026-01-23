///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.zeroturnaround:zt-exec:1.12
//DEPS com.fasterxml.jackson.core:jackson-databind:2.17.1
//JAVA 17
//FILES ExampleInfo.json
//SOURCES ../../../../integration-testing/jbang-lib/IntegrationTestUtils.java
//SOURCES ../../../../integration-testing/jbang-lib/WebServerTestUtils.java
//SOURCES ../../../../integration-testing/jbang-lib/McpTestUtils.java

/*
 * Integration test for weather/starter-webmvc-server
 */
public class RunWeatherStarterWebmvcServerWebServer {
    public static void main(String... args) throws Exception {
        McpTestUtils.runMcpWeatherServerTest("weather-starter-webmvc-server", 15, "http://localhost:8080/mcp");
    }
}
