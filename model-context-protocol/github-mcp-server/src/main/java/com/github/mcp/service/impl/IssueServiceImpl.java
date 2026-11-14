package com.github.mcp.service.impl;

import com.github.mcp.client.GitHubRestClient;
import com.github.mcp.dto.common.ApiResponse;
import com.github.mcp.dto.common.PagedResponse;
import com.github.mcp.dto.request.CreateIssueRequest;
import com.github.mcp.dto.request.UpdateIssueRequest;
import com.github.mcp.dto.response.IssueResponse;
import com.github.mcp.service.interfaces.IssueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class IssueServiceImpl implements IssueService {

    private final GitHubRestClient gitHubClient;

    @Override
    public ApiResponse<IssueResponse> createIssue(String owner, String repo, CreateIssueRequest request) {
        try {
            String path = String.format("/repos/%s/%s/issues", owner, repo);
            IssueResponse response = gitHubClient.post(path, request, IssueResponse.class);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("Error creating issue: {}", e.getMessage());
            return ApiResponse.error("CREATE_ERROR", "Failed to create issue: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<IssueResponse> updateIssue(String owner, String repo, Long issueNumber, UpdateIssueRequest request) {
        try {
            String path = String.format("/repos/%s/%s/issues/%d", owner, repo, issueNumber);
            IssueResponse response = gitHubClient.patch(path, request, IssueResponse.class);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("Error updating issue: {}", e.getMessage());
            return ApiResponse.error("UPDATE_ERROR", "Failed to update issue: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<IssueResponse> addIssueComment(String owner, String repo, Long issueNumber, String body) {
        try {
            String path = String.format("/repos/%s/%s/issues/%d/comments", owner, repo, issueNumber);
            Map<String, String> request = new HashMap<>();
            request.put("body", body);
            IssueResponse response = gitHubClient.post(path, request, IssueResponse.class);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("Error adding issue comment: {}", e.getMessage());
            return ApiResponse.error("COMMENT_ERROR", "Failed to add comment: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<PagedResponse<IssueResponse>> listIssues(String owner, String repo, String state, String labels, String assignee, Integer page, Integer perPage) {
        try {
            String url = String.format("/repos/%s/%s/issues", owner, repo);
            Map<String, Object> params = new HashMap<>();
            if (state != null) params.put("state", state);
            if (labels != null) params.put("labels", labels);
            if (assignee != null) params.put("assignee", assignee);
            params.put("page", page != null ? page : 1);
            params.put("per_page", perPage != null ? perPage : 30);
            
            ResponseEntity<String> rawResponse = gitHubClient.getRawResponse(url, params);
            List<IssueResponse> issues = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(rawResponse.getBody(), new TypeReference<List<IssueResponse>>() {});
            
            return ApiResponse.success(PagedResponse.of(issues, page != null ? page : 1, perPage != null ? perPage : 30, issues.size()));
        } catch (Exception e) {
            log.error("Error listing issues: {}", e.getMessage());
            return ApiResponse.error("LIST_ERROR", "Failed to list issues: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<IssueResponse> getIssue(String owner, String repo, Long issueNumber) {
        try {
            String path = String.format("/repos/%s/%s/issues/%d", owner, repo, issueNumber);
            IssueResponse response = gitHubClient.get(path, IssueResponse.class);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("Error getting issue: {}", e.getMessage());
            return ApiResponse.error("GET_ERROR", "Failed to get issue: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<IssueResponse> closeIssue(String owner, String repo, Long issueNumber) {
        try {
            String path = String.format("/repos/%s/%s/issues/%d", owner, repo, issueNumber);
            Map<String, String> body = new HashMap<>();
            body.put("state", "closed");
            IssueResponse response = gitHubClient.patch(path, body, IssueResponse.class);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("Error closing issue: {}", e.getMessage());
            return ApiResponse.error("CLOSE_ERROR", "Failed to close issue: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<IssueResponse> reopenIssue(String owner, String repo, Long issueNumber) {
        try {
            String path = String.format("/repos/%s/%s/issues/%d", owner, repo, issueNumber);
            Map<String, String> body = new HashMap<>();
            body.put("state", "open");
            IssueResponse response = gitHubClient.patch(path, body, IssueResponse.class);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("Error reopening issue: {}", e.getMessage());
            return ApiResponse.error("REOPEN_ERROR", "Failed to reopen issue: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<IssueResponse> assignIssue(String owner, String repo, Long issueNumber, List<String> assignees) {
        try {
            String path = String.format("/repos/%s/%s/issues/%d/assignees", owner, repo, issueNumber);
            Map<String, List<String>> body = new HashMap<>();
            body.put("assignees", assignees);
            IssueResponse response = gitHubClient.post(path, body, IssueResponse.class);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("Error assigning issue: {}", e.getMessage());
            return ApiResponse.error("ASSIGN_ERROR", "Failed to assign issue: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<IssueResponse> unassignIssue(String owner, String repo, Long issueNumber) {
        try {
            String path = String.format("/repos/%s/%s/issues/%d/assignees", owner, repo, issueNumber);
            Map<String, List<String>> body = new HashMap<>();
            body.put("assignees", List.of());
            IssueResponse response = gitHubClient.post(path, body, IssueResponse.class);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("Error unassigning issue: {}", e.getMessage());
            return ApiResponse.error("UNASSIGN_ERROR", "Failed to unassign issue: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<IssueResponse> addIssueLabels(String owner, String repo, Long issueNumber, List<String> labels) {
        try {
            String path = String.format("/repos/%s/%s/issues/%d/labels", owner, repo, issueNumber);
            Map<String, List<String>> body = new HashMap<>();
            body.put("labels", labels);
            IssueResponse response = gitHubClient.post(path, body, IssueResponse.class);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("Error adding labels: {}", e.getMessage());
            return ApiResponse.error("LABEL_ERROR", "Failed to add labels: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<IssueResponse> removeIssueLabel(String owner, String repo, Long issueNumber, String label) {
        try {
            String path = String.format("/repos/%s/%s/issues/%d/labels/%s", owner, repo, issueNumber, label);
            gitHubClient.delete(path);
            IssueResponse issue = getIssue(owner, repo, issueNumber).getData();
            return ApiResponse.success(issue);
        } catch (Exception e) {
            log.error("Error removing label: {}", e.getMessage());
            return ApiResponse.error("LABEL_ERROR", "Failed to remove label: " + e.getMessage());
        }
    }
}
