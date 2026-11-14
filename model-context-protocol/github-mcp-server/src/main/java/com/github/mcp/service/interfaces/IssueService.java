package com.github.mcp.service.interfaces;

import com.github.mcp.dto.common.ApiResponse;
import com.github.mcp.dto.common.PagedResponse;
import com.github.mcp.dto.request.CreateIssueRequest;
import com.github.mcp.dto.request.UpdateIssueRequest;
import com.github.mcp.dto.response.IssueResponse;

import java.util.List;

public interface IssueService {
    ApiResponse<IssueResponse> createIssue(String owner, String repo, CreateIssueRequest request);

    ApiResponse<IssueResponse> updateIssue(String owner, String repo, Long issueNumber, UpdateIssueRequest request);

    ApiResponse<IssueResponse> addIssueComment(String owner, String repo, Long issueNumber, String body);

    ApiResponse<PagedResponse<IssueResponse>> listIssues(String owner, String repo, String state, String labels, String assignee, Integer page, Integer perPage);

    ApiResponse<IssueResponse> getIssue(String owner, String repo, Long issueNumber);

    ApiResponse<IssueResponse> closeIssue(String owner, String repo, Long issueNumber);

    ApiResponse<IssueResponse> reopenIssue(String owner, String repo, Long issueNumber);

    ApiResponse<IssueResponse> assignIssue(String owner, String repo, Long issueNumber, List<String> assignees);

    ApiResponse<IssueResponse> unassignIssue(String owner, String repo, Long issueNumber);

    ApiResponse<IssueResponse> addIssueLabels(String owner, String repo, Long issueNumber, List<String> labels);

    ApiResponse<IssueResponse> removeIssueLabel(String owner, String repo, Long issueNumber, String label);
}
