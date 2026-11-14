package com.github.mcp.tools;

import com.github.mcp.client.GitHubRestClient;
import com.github.mcp.dto.common.ApiResponse;
import com.github.mcp.dto.common.PagedResponse;
import com.github.mcp.dto.response.RepositoryResponse;
import com.github.mcp.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserTools {

    private final GitHubRestClient gitHubClient;

    @McpTool(name = "get_authenticated_user", description = "Get information about the authenticated user")
    public ApiResponse<UserResponse> getAuthenticatedUser() {
        try {
            UserResponse response = gitHubClient.get("/user", UserResponse.class);
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error("GET_ERROR", "Failed to get user: " + e.getMessage());
        }
    }

    @McpTool(name = "get_user", description = "Get information about a GitHub user")
    public ApiResponse<UserResponse> getUser(
            @McpToolParam(description = "Username", required = true) String username) {
        try {
            String path = String.format("/users/%s", username);
            UserResponse response = gitHubClient.get(path, UserResponse.class);
            return ApiResponse.success(response);
        } catch (Exception e) {
            return ApiResponse.error("GET_ERROR", "Failed to get user: " + e.getMessage());
        }
    }

    @McpTool(name = "list_user_repositories", description = "List repositories for a user")
    public ApiResponse<PagedResponse<RepositoryResponse>> listUserRepositories(
            @McpToolParam(description = "Username (omit for authenticated user)") String username,
            @McpToolParam(description = "Filter: all, owner, member") String type,
            @McpToolParam(description = "Sort: created, updated, pushed, full_name") String sort,
            @McpToolParam(description = "Direction: asc, desc") String direction,
            @McpToolParam(description = "Page number") Integer page,
            @McpToolParam(description = "Items per page (max 100)") Integer perPage) {
        try {
            String path = username != null ? String.format("/users/%s/repos", username) : "/user/repos";
            Map<String, Object> params = new HashMap<>();
            if (type != null) params.put("type", type);
            if (sort != null) params.put("sort", sort);
            if (direction != null) params.put("direction", direction);
            params.put("page", page != null ? page : 1);
            params.put("per_page", perPage != null ? perPage : 30);
            
            ResponseEntity<String> rawResponse = gitHubClient.getRawResponse(path, params);
            List<RepositoryResponse> repos = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(rawResponse.getBody(), new TypeReference<List<RepositoryResponse>>() {});
            
            return ApiResponse.success(PagedResponse.of(repos, page != null ? page : 1, perPage != null ? perPage : 30, repos.size()));
        } catch (Exception e) {
            return ApiResponse.error("LIST_ERROR", "Failed to list repositories: " + e.getMessage());
        }
    }
}
