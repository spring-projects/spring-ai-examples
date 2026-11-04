package com.github.mcp.exception;

import com.github.mcp.dto.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GitHubMcpException.class)
    public ResponseEntity<ApiResponse<Void>> handleGitHubMcpException(GitHubMcpException ex) {
        log.error("GitHub MCP Exception: {}", ex.getMessage());
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(ApiResponse.error(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpClientError(HttpClientErrorException ex) {
        log.error("HTTP Client Error: {}", ex.getMessage());
        String code = "HTTP_" + ex.getStatusCode().value();
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(ApiResponse.error(code, extractGitHubErrorMessage(ex)));
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpServerError(HttpServerErrorException ex) {
        log.error("HTTP Server Error: {}", ex.getMessage());
        String code = "HTTP_" + ex.getStatusCode().value();
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(ApiResponse.error(code, "GitHub API server error"));
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceAccess(ResourceAccessException ex) {
        log.error("Resource Access Error: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("CONNECTION_ERROR", "Unable to connect to GitHub API"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "An unexpected error occurred"));
    }

    private String extractGitHubErrorMessage(HttpClientErrorException ex) {
        try {
            String responseBody = ex.getResponseBodyAsString();
            if (responseBody != null && responseBody.contains("message")) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                var jsonNode = mapper.readTree(responseBody);
                if (jsonNode.has("message")) {
                    return jsonNode.get("message").asText();
                }
            }
        } catch (Exception e) {
            log.debug("Could not parse error response");
        }
        return ex.getMessage();
    }
}
