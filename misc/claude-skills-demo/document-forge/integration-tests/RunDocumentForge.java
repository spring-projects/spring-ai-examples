///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.zeroturnaround:zt-exec:1.12
//DEPS com.fasterxml.jackson.core:jackson-databind:2.17.1
//JAVA 17
//FILES ExampleInfo.json
//SOURCES ../../../../integration-testing/jbang-lib/IntegrationTestUtils.java
//SOURCES ../../../../integration-testing/jbang-lib/WebServerTestUtils.java

/*
 * Integration test launcher for document-forge
 * Tests Claude Skills document generation web application
 */

public class RunDocumentForge {

    public static void main(String... args) throws Exception {
        IntegrationTestUtils.runIntegrationTest("document-forge");
    }
}
