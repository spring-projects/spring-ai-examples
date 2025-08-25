package org.springframework.ai.mcp.sample.server;

import java.util.List;

import org.springaicommunity.mcp.spring.SyncMcpAnnotationProviders;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;

@SpringBootApplication
public class McpServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(McpServerApplication.class, args);
	}

	@Bean
	public List<SyncToolSpecification> toolSpecs(WeatherService weatherService) {
		return SyncMcpAnnotationProviders.toolSpecifications(List.of(weatherService));
	}

}
