package com.github.mcp.service.interfaces;

import com.github.mcp.dto.common.ApiResponse;
import com.github.mcp.dto.common.PagedResponse;
import com.github.mcp.dto.request.CreatePullRequestRequest;
import com.github.mcp.dto.request.MergePullRequestRequest;
import com.github.mcp.dto.request.UpdatePullRequestRequest;
import com.github.mcp.dto.response.PullRequestResponse;

public interface PullRequestService {
    ApiResponse<PullRequestResponse> createPullRequest(String owner, String repo, CreatePullRequestRequest request);

    ApiResponse<PullRequestResponse> updatePullRequest(String owner, String repo, Long pullNumber, UpdatePullRequestRequest request);

    ApiResponse<PagedResponse<PullRequestResponse>> listPullRequests(String owner, String repo, String state, String head, String base, String sort, String direction, Integer page, Integer perPage);

    ApiResponse<PullRequestResponse> getPullRequest(String owner, String repo, Long pullNumber);

    ApiResponse<PullRequestResponse> mergePullRequest(String owner, String repo, Long pullNumber, MergePullRequestRequest request);

    ApiResponse<PullRequestResponse> closePullRequest(String owner, String repo, Long pullNumber);

    ApiResponse<PullRequestResponse> reopenPullRequest(String owner, String repo, Long pullNumber);

    ApiResponse<PullRequestResponse> addPullRequestComment(String owner, String repo, Long pullNumber, String body, String commitId, String path, Integer position);

    ApiResponse<Object> createPullRequestReview(String owner, String repo, Long pullNumber, String body, String event, Object comments);

    ApiResponse<Object> submitPullRequestReview(String owner, String repo, Long pullNumber, Long reviewId, String body, String event);
}
