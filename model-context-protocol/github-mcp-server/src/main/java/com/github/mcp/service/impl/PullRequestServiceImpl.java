package com.github.mcp.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mcp.client.GitHubRestClient;
import com.github.mcp.dto.common.ApiResponse;
import com.github.mcp.dto.common.PagedResponse;
import com.github.mcp.dto.request.CreatePullRequestRequest;
import com.github.mcp.dto.request.MergePullRequestRequest;
import com.github.mcp.dto.request.UpdatePullRequestRequest;
import com.github.mcp.dto.response.PullRequestResponse;
import com.github.mcp.service.interfaces.PullRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PullRequestServiceImpl implements PullRequestService {

    private final GitHubRestClient gitHubClient;
    private final ObjectMapper objectMapper;

    @Override
    public ApiResponse<PullRequestResponse> createPullRequest(String owner, String repo, CreatePullRequestRequest request) {
        try {
            String path = String.format("/repos/%s/%s/pulls", owner, repo);
            PullRequestResponse response = gitHubClient.post(path, request, PullRequestResponse.class);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("Error creating pull request: {}", e.getMessage());
            return ApiResponse.error("CREATE_ERROR", "Failed to create pull request: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<PullRequestResponse> updatePullRequest(String owner, String repo, Long pullNumber, UpdatePullRequestRequest request) {
        try {
            String path = String.format("/repos/%s/%s/pulls/%d", owner, repo, pullNumber);
            PullRequestResponse response = gitHubClient.patch(path, request, PullRequestResponse.class);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("Error updating pull request: {}", e.getMessage());
            return ApiResponse.error("UPDATE_ERROR", "Failed to update pull request: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<PagedResponse<PullRequestResponse>> listPullRequests(String owner, String repo, String state, String head, String base, String sort, String direction, Integer page, Integer perPage) {
        try {
            String url = String.format("/repos/%s/%s/pulls", owner, repo);
            Map<String, Object> params = new HashMap<>();
            if (state != null) params.put("state", state);
            if (head != null) params.put("head", head);
            if (base != null) params.put("base", base);
            if (sort != null) params.put("sort", sort);
            if (direction != null) params.put("direction", direction);
            params.put("page", page != null ? page : 1);
            params.put("per_page", perPage != null ? perPage : 30);
            
            ResponseEntity<String> rawResponse = gitHubClient.getRawResponse(url, params);
            List<PullRequestResponse> pulls = objectMapper.readerForListOf(PullRequestResponse.class)
                    .readValue(rawResponse.getBody());
            
            return ApiResponse.success(PagedResponse.of(pulls, page != null ? page : 1, perPage != null ? perPage : 30, pulls.size()));
        } catch (Exception e) {
            log.error("Error listing pull requests: {}", e.getMessage());
            return ApiResponse.error("LIST_ERROR", "Failed to list pull requests: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<PullRequestResponse> getPullRequest(String owner, String repo, Long pullNumber) {
        try {
            String path = String.format("/repos/%s/%s/pulls/%d", owner, repo, pullNumber);
            PullRequestResponse response = gitHubClient.get(path, PullRequestResponse.class);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("Error getting pull request: {}", e.getMessage());
            return ApiResponse.error("GET_ERROR", "Failed to get pull request: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<PullRequestResponse> mergePullRequest(String owner, String repo, Long pullNumber, MergePullRequestRequest request) {
        try {
            String path = String.format("/repos/%s/%s/pulls/%d/merge", owner, repo, pullNumber);
            Map<String, Object> response = gitHubClient.put(path, request, Map.class);
            PullRequestResponse pr = getPullRequest(owner, repo, pullNumber).getData();
            return ApiResponse.success(pr);
        } catch (Exception e) {
            log.error("Error merging pull request: {}", e.getMessage());
            return ApiResponse.error("MERGE_ERROR", "Failed to merge pull request: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<PullRequestResponse> closePullRequest(String owner, String repo, Long pullNumber) {
        try {
            String path = String.format("/repos/%s/%s/pulls/%d", owner, repo, pullNumber);
            Map<String, String> body = new HashMap<>();
            body.put("state", "closed");
            PullRequestResponse response = gitHubClient.patch(path, body, PullRequestResponse.class);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("Error closing pull request: {}", e.getMessage());
            return ApiResponse.error("CLOSE_ERROR", "Failed to close pull request: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<PullRequestResponse> reopenPullRequest(String owner, String repo, Long pullNumber) {
        try {
            String path = String.format("/repos/%s/%s/pulls/%d", owner, repo, pullNumber);
            Map<String, String> body = new HashMap<>();
            body.put("state", "open");
            PullRequestResponse response = gitHubClient.patch(path, body, PullRequestResponse.class);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("Error reopening pull request: {}", e.getMessage());
            return ApiResponse.error("REOPEN_ERROR", "Failed to reopen pull request: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<PullRequestResponse> addPullRequestComment(String owner, String repo, Long pullNumber, String body, String commitId, String path, Integer position) {
        try {
            String apiPath = String.format("/repos/%s/%s/pulls/%d/comments", owner, repo, pullNumber);
            Map<String, Object> request = new HashMap<>();
            request.put("body", body);
            if (commitId != null) request.put("commit_id", commitId);
            if (path != null) request.put("path", path);
            if (position != null) request.put("position", position);
            
            PullRequestResponse response = gitHubClient.post(apiPath, request, PullRequestResponse.class);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("Error adding pull request comment: {}", e.getMessage());
            return ApiResponse.error("COMMENT_ERROR", "Failed to add comment: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<Object> createPullRequestReview(String owner, String repo, Long pullNumber, String body, String event, Object comments) {
        try {
            String path = String.format("/repos/%s/%s/pulls/%d/reviews", owner, repo, pullNumber);
            Map<String, Object> request = new HashMap<>();
            if (body != null) request.put("body", body);
            if (event != null) request.put("event", event);
            if (comments != null) request.put("comments", comments);
            
            Object response = gitHubClient.post(path, request, Object.class);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("Error creating pull request review: {}", e.getMessage());
            return ApiResponse.error("REVIEW_ERROR", "Failed to create review: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<Object> submitPullRequestReview(String owner, String repo, Long pullNumber, Long reviewId, String body, String event) {
        try {
            String path = String.format("/repos/%s/%s/pulls/%d/reviews/%d/events", owner, repo, pullNumber, reviewId);
            Map<String, Object> request = new HashMap<>();
            if (body != null) request.put("body", body);
            if (event != null) request.put("event", event);
            
            Object response = gitHubClient.post(path, request, Object.class);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("Error submitting pull request review: {}", e.getMessage());
            return ApiResponse.error("REVIEW_ERROR", "Failed to submit review: " + e.getMessage());
        }
    }
}
