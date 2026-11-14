package com.github.mcp.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mcp.config.GitHubProperties;
import com.github.mcp.exception.GitHubMcpException;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GitHubRestClient {

    private final RestTemplate restTemplate;
    private final GitHubProperties properties;
    private final ObjectMapper objectMapper;

    @Getter
    private String baseUrl;

    @PostConstruct
    public void init() {
        if ("github.com".equals(properties.getHost())) {
            this.baseUrl = "https://api.github.com";
        } else {
            this.baseUrl = "https://" + properties.getHost() + "/api/v3";
        }
        log.info("GitHub REST API base URL: {}", baseUrl);
    }

    public <T> T get(String path, Class<T> responseType) {
        return get(path, null, responseType);
    }

    public <T> T get(String path, Map<String, Object> params, Class<T> responseType) {
        URI uri = buildUri(path, params);
        return exchange(uri, HttpMethod.GET, null, responseType);
    }

    public <T> T post(String path, Object body, Class<T> responseType) {
        URI uri = buildUri(path, null);
        return exchange(uri, HttpMethod.POST, body, responseType);
    }

    public <T> T put(String path, Object body, Class<T> responseType) {
        URI uri = buildUri(path, null);
        return exchange(uri, HttpMethod.PUT, body, responseType);
    }

    public <T> T patch(String path, Object body, Class<T> responseType) {
        URI uri = buildUri(path, null);
        return exchange(uri, HttpMethod.PATCH, body, responseType);
    }

    public void delete(String path) {
        URI uri = buildUri(path, null);
        HttpEntity<Void> entity = new HttpEntity<>(buildHeaders());
        restTemplate.exchange(uri, HttpMethod.DELETE, entity, Void.class);
    }

    public ResponseEntity<String> getRawResponse(String path, Map<String, Object> params) {
        URI uri = buildUri(path, params);
        HttpEntity<Void> entity = new HttpEntity<>(buildHeaders());
        try {
            return restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
        } catch (RestClientResponseException ex) {
            throw buildGitHubException(HttpMethod.GET, uri, ex);
        }
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON, MediaType.valueOf("application/vnd.github+json")));
        if (StringUtils.hasText(properties.getPersonalAccessToken())) {
            headers.setBearerAuth(properties.getPersonalAccessToken());
        }
        headers.set("User-Agent", "github-mcp-server/1.0.0");
        headers.set("X-GitHub-Api-Version", "2022-11-28");
        return headers;
    }

    private <T> T exchange(URI uri, HttpMethod method, Object body, Class<T> responseType) {
        HttpEntity<?> entity = new HttpEntity<>(body, buildHeaders());
        try {
            ResponseEntity<String> response = restTemplate.exchange(uri, method, entity, String.class);
            if (responseType == Void.class || response.getBody() == null || response.getBody().isBlank()) {
                return null;
            }
            return objectMapper.readValue(response.getBody(), responseType);
        } catch (RestClientResponseException ex) {
            throw buildGitHubException(method, uri, ex);
        } catch (Exception ex) {
            throw new GitHubMcpException("DESERIALIZATION_ERROR",
                    String.format("Failed to parse GitHub response for %s %s: %s", method, uri, ex.getMessage()),
                    500);
        }
    }

    private GitHubMcpException buildGitHubException(HttpMethod method, URI uri, RestClientResponseException ex) {
        String body = ex.getResponseBodyAsString();
        String message = String.format("GitHub API %s %s failed with %d %s",
                method,
                uri,
                ex.getStatusCode().value(),
                ex.getStatusText());
        if (StringUtils.hasText(body)) {
            message += ": " + body;
        }
        return new GitHubMcpException("HTTP_" + ex.getStatusCode().value(), message, ex.getStatusCode().value());
    }

    private URI buildUri(String path, Map<String, Object> params) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl + path);
        if (params != null) {
            params.forEach((key, value) -> {
                if (value != null) {
                    builder.queryParam(key, value);
                }
            });
        }
        return builder.build().toUri();
    }
}
