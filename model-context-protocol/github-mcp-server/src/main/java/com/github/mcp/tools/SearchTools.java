package com.github.mcp.tools;

import com.github.mcp.client.GitHubRestClient;
import com.github.mcp.dto.common.ApiResponse;
import com.github.mcp.dto.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SearchTools {

    private final GitHubRestClient gitHubClient;

    @McpTool(name = "search_code", description = "Search for code across GitHub repositories")
    public ApiResponse<SearchResultResponse<com.fasterxml.jackson.databind.JsonNode>> searchCode(
            @McpToolParam(description = "Search query (see GitHub search syntax)", required = true) String query,
            @McpToolParam(description = "Sort field: indexed") String sort,
            @McpToolParam(description = "Sort order: asc, desc") String order,
            @McpToolParam(description = "Page number") Integer page,
            @McpToolParam(description = "Items per page (max 100)") Integer perPage) {
        
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("q", query);
            if (sort != null) params.put("sort", sort);
            if (order != null) params.put("order", order);
            params.put("page", page != null ? page : 1);
            params.put("per_page", perPage != null ? perPage : 30);
            
            ResponseEntity<String> rawResponse = gitHubClient.getRawResponse("/search/code", params);
            SearchResultResponse<com.fasterxml.jackson.databind.JsonNode> result = 
                new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                    rawResponse.getBody(), 
                    new TypeReference<SearchResultResponse<com.fasterxml.jackson.databind.JsonNode>>() {}
                );
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("SEARCH_ERROR", "Failed to search code: " + e.getMessage());
        }
    }

    @McpTool(name = "search_issues", description = "Search for issues and pull requests across GitHub")
    public ApiResponse<SearchResultResponse<IssueResponse>> searchIssues(
            @McpToolParam(description = "Search query (see GitHub search syntax)", required = true) String query,
            @McpToolParam(description = "Sort field: comments, reactions, created, updated, interactions") String sort,
            @McpToolParam(description = "Sort order: asc, desc") String order,
            @McpToolParam(description = "Page number") Integer page,
            @McpToolParam(description = "Items per page (max 100)") Integer perPage) {
        
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("q", query);
            if (sort != null) params.put("sort", sort);
            if (order != null) params.put("order", order);
            params.put("page", page != null ? page : 1);
            params.put("per_page", perPage != null ? perPage : 30);
            
            ResponseEntity<String> rawResponse = gitHubClient.getRawResponse("/search/issues", params);
            SearchResultResponse<IssueResponse> result = 
                new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                    rawResponse.getBody(), 
                    new TypeReference<SearchResultResponse<IssueResponse>>() {}
                );
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("SEARCH_ERROR", "Failed to search issues: " + e.getMessage());
        }
    }

    @McpTool(name = "search_repositories", description = "Search for repositories on GitHub")
    public ApiResponse<SearchResultResponse<RepositoryResponse>> searchRepositories(
            @McpToolParam(description = "Search query (see GitHub search syntax)", required = true) String query,
            @McpToolParam(description = "Sort field: stars, forks, help-wanted-issues, updated") String sort,
            @McpToolParam(description = "Sort order: asc, desc") String order,
            @McpToolParam(description = "Page number") Integer page,
            @McpToolParam(description = "Items per page (max 100)") Integer perPage) {
        
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("q", query);
            if (sort != null) params.put("sort", sort);
            if (order != null) params.put("order", order);
            params.put("page", page != null ? page : 1);
            params.put("per_page", perPage != null ? perPage : 30);
            
            ResponseEntity<String> rawResponse = gitHubClient.getRawResponse("/search/repositories", params);
            SearchResultResponse<RepositoryResponse> result = 
                new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                    rawResponse.getBody(), 
                    new TypeReference<SearchResultResponse<RepositoryResponse>>() {}
                );
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("SEARCH_ERROR", "Failed to search repositories: " + e.getMessage());
        }
    }

    @McpTool(name = "search_users", description = "Search for users on GitHub")
    public ApiResponse<SearchResultResponse<UserResponse>> searchUsers(
            @McpToolParam(description = "Search query (see GitHub search syntax)", required = true) String query,
            @McpToolParam(description = "Sort field: followers, repositories, joined") String sort,
            @McpToolParam(description = "Sort order: asc, desc") String order,
            @McpToolParam(description = "Page number") Integer page,
            @McpToolParam(description = "Items per page (max 100)") Integer perPage) {
        
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("q", query);
            if (sort != null) params.put("sort", sort);
            if (order != null) params.put("order", order);
            params.put("page", page != null ? page : 1);
            params.put("per_page", perPage != null ? perPage : 30);
            
            ResponseEntity<String> rawResponse = gitHubClient.getRawResponse("/search/users", params);
            SearchResultResponse<UserResponse> result = 
                new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                    rawResponse.getBody(), 
                    new TypeReference<SearchResultResponse<UserResponse>>() {}
                );
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("SEARCH_ERROR", "Failed to search users: " + e.getMessage());
        }
    }
}
