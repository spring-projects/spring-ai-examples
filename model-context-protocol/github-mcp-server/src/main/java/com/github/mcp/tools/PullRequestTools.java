package com.github.mcp.tools;

import com.github.mcp.dto.common.ApiResponse;
import com.github.mcp.dto.common.PagedResponse;
import com.github.mcp.dto.request.CreatePullRequestRequest;
import com.github.mcp.dto.request.MergePullRequestRequest;
import com.github.mcp.dto.request.UpdatePullRequestRequest;
import com.github.mcp.dto.response.PullRequestResponse;
import com.github.mcp.service.interfaces.PullRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PullRequestTools {

    private final PullRequestService pullRequestService;

    @McpTool(name = "create_pull_request", description = "Create a new pull request")
    public ApiResponse<PullRequestResponse> createPullRequest(
            @McpToolParam(description = "Repository owner", required = true) String owner,
            @McpToolParam(description = "Repository name", required = true) String repo,
            @McpToolParam(description = "Pull request title", required = true) String title,
            @McpToolParam(description = "Pull request body/description") String body,
            @McpToolParam(description = "Branch containing changes", required = true) String head,
            @McpToolParam(description = "Branch to merge into", required = true) String base,
            @McpToolParam(description = "Create as draft PR") Boolean draft) {
        
        CreatePullRequestRequest request = CreatePullRequestRequest.builder()
                .title(title)
                .body(body)
                .head(head)
                .base(base)
                .draft(draft)
                .build();
        
        return pullRequestService.createPullRequest(owner, repo, request);
    }

    @McpTool(name = "update_pull_request", description = "Update an existing pull request")
    public ApiResponse<PullRequestResponse> updatePullRequest(
            @McpToolParam(description = "Repository owner", required = true) String owner,
            @McpToolParam(description = "Repository name", required = true) String repo,
            @McpToolParam(description = "Pull request number", required = true) Long pullNumber,
            @McpToolParam(description = "New title") String title,
            @McpToolParam(description = "New body/description") String body,
            @McpToolParam(description = "New base branch") String base) {
        
        UpdatePullRequestRequest request = UpdatePullRequestRequest.builder()
                .title(title)
                .body(body)
                .base(base)
                .build();
        
        return pullRequestService.updatePullRequest(owner, repo, pullNumber, request);
    }

    @McpTool(name = "list_pull_requests", description = "List pull requests in a repository")
    public ApiResponse<PagedResponse<PullRequestResponse>> listPullRequests(
            @McpToolParam(description = "Repository owner", required = true) String owner,
            @McpToolParam(description = "Repository name", required = true) String repo,
            @McpToolParam(description = "PR state: open, closed, all") String state,
            @McpToolParam(description = "Filter by head branch (format: user:branch)") String head,
            @McpToolParam(description = "Filter by base branch") String base,
            @McpToolParam(description = "Sort field: created, updated, popularity, long-running") String sort,
            @McpToolParam(description = "Sort direction: asc, desc") String direction,
            @McpToolParam(description = "Page number") Integer page,
            @McpToolParam(description = "Items per page (max 100)") Integer perPage) {
        
        return pullRequestService.listPullRequests(owner, repo, state, head, base, sort, direction, page, perPage);
    }

    @McpTool(name = "get_pull_request", description = "Get details of a specific pull request")
    public ApiResponse<PullRequestResponse> getPullRequest(
            @McpToolParam(description = "Repository owner", required = true) String owner,
            @McpToolParam(description = "Repository name", required = true) String repo,
            @McpToolParam(description = "Pull request number", required = true) Long pullNumber) {
        
        return pullRequestService.getPullRequest(owner, repo, pullNumber);
    }

    @McpTool(name = "merge_pull_request", description = "Merge a pull request")
    public ApiResponse<PullRequestResponse> mergePullRequest(
            @McpToolParam(description = "Repository owner", required = true) String owner,
            @McpToolParam(description = "Repository name", required = true) String repo,
            @McpToolParam(description = "Pull request number", required = true) Long pullNumber,
            @McpToolParam(description = "Commit title") String commitTitle,
            @McpToolParam(description = "Commit message") String commitMessage,
            @McpToolParam(description = "SHA that pull request head must match") String sha,
            @McpToolParam(description = "Merge method: merge, squash, rebase") String mergeMethod) {
        
        MergePullRequestRequest request = MergePullRequestRequest.builder()
                .commitTitle(commitTitle)
                .commitMessage(commitMessage)
                .sha(sha)
                .mergeMethod(mergeMethod != null ? mergeMethod : "merge")
                .build();
        
        return pullRequestService.mergePullRequest(owner, repo, pullNumber, request);
    }

    @McpTool(name = "close_pull_request", description = "Close a pull request without merging")
    public ApiResponse<PullRequestResponse> closePullRequest(
            @McpToolParam(description = "Repository owner", required = true) String owner,
            @McpToolParam(description = "Repository name", required = true) String repo,
            @McpToolParam(description = "Pull request number", required = true) Long pullNumber) {
        
        return pullRequestService.closePullRequest(owner, repo, pullNumber);
    }

    @McpTool(name = "reopen_pull_request", description = "Reopen a closed pull request")
    public ApiResponse<PullRequestResponse> reopenPullRequest(
            @McpToolParam(description = "Repository owner", required = true) String owner,
            @McpToolParam(description = "Repository name", required = true) String repo,
            @McpToolParam(description = "Pull request number", required = true) Long pullNumber) {
        
        return pullRequestService.reopenPullRequest(owner, repo, pullNumber);
    }

    @McpTool(name = "add_pull_request_comment", description = "Add a comment to a pull request")
    public ApiResponse<PullRequestResponse> addPullRequestComment(
            @McpToolParam(description = "Repository owner", required = true) String owner,
            @McpToolParam(description = "Repository name", required = true) String repo,
            @McpToolParam(description = "Pull request number", required = true) Long pullNumber,
            @McpToolParam(description = "Comment body", required = true) String body,
            @McpToolParam(description = "Commit SHA for review comment") String commitId,
            @McpToolParam(description = "File path for review comment") String path,
            @McpToolParam(description = "Line position for review comment") Integer position) {
        
        return pullRequestService.addPullRequestComment(owner, repo, pullNumber, body, commitId, path, position);
    }

    @McpTool(name = "create_pull_request_review", description = "Create a review on a pull request")
    public ApiResponse<Object> createPullRequestReview(
            @McpToolParam(description = "Repository owner", required = true) String owner,
            @McpToolParam(description = "Repository name", required = true) String repo,
            @McpToolParam(description = "Pull request number", required = true) Long pullNumber,
            @McpToolParam(description = "Review body") String body,
            @McpToolParam(description = "Review event: APPROVE, REQUEST_CHANGES, COMMENT") String event) {
        
        return pullRequestService.createPullRequestReview(owner, repo, pullNumber, body, event, null);
    }

    @McpTool(name = "submit_pull_request_review", description = "Submit a pending pull request review")
    public ApiResponse<Object> submitPullRequestReview(
            @McpToolParam(description = "Repository owner", required = true) String owner,
            @McpToolParam(description = "Repository name", required = true) String repo,
            @McpToolParam(description = "Pull request number", required = true) Long pullNumber,
            @McpToolParam(description = "Review ID", required = true) Long reviewId,
            @McpToolParam(description = "Review body") String body,
            @McpToolParam(description = "Review event: APPROVE, REQUEST_CHANGES, COMMENT") String event) {
        
        return pullRequestService.submitPullRequestReview(owner, repo, pullNumber, reviewId, body, event);
    }
}
