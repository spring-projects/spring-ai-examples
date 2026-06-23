package com.github.mcp.client;

import com.github.mcp.config.GitHubProperties;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GitHubGraphqlClient {

    private final RestTemplate restTemplate;
    private final GitHubProperties properties;

    @Getter
    private String graphqlUrl;

    @PostConstruct
    public void init() {
        if ("github.com".equals(properties.getHost())) {
            this.graphqlUrl = "https://api.github.com/graphql";
        } else {
            this.graphqlUrl = "https://" + properties.getHost() + "/api/graphql";
        }
        log.info("GitHub GraphQL API URL: {}", graphqlUrl);
    }

    public <T> T execute(String query, Map<String, Object> variables, Class<T> responseType) {
        GraphqlRequest request = new GraphqlRequest(query, variables);
        HttpEntity<GraphqlRequest> entity = new HttpEntity<>(request, buildHeaders());
        ResponseEntity<T> response = restTemplate.exchange(
                URI.create(graphqlUrl),
                HttpMethod.POST,
                entity,
                responseType
        );
        return response.getBody();
    }

    public <T> T execute(String query, Class<T> responseType) {
        return execute(query, null, responseType);
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Bearer " + properties.getPersonalAccessToken());
        headers.set("User-Agent", "github-mcp-server/1.0.0");
        return headers;
    }

    public record GraphqlRequest(String query, Map<String, Object> variables) {
    }
}
