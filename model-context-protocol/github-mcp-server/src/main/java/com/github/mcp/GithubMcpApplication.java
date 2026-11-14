package com.github.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class GithubMcpApplication {
    public static void main(String[] args) {
        SpringApplication.run(GithubMcpApplication.class, args);
    }
}
