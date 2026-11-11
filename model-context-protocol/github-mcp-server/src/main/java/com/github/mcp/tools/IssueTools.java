package com.github.mcp.tools;

import com.github.mcp.dto.common.ApiResponse;
import com.github.mcp.dto.common.PagedResponse;
import com.github.mcp.dto.request.CreateIssueRequest;
import com.github.mcp.dto.request.UpdateIssueRequest;
import com.github.mcp.dto.response.IssueResponse;
import com.github.mcp.service.interfaces.IssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class IssueTools {

    private final IssueService issueService;

    @McpTool(name = "create_issue", description = "Create a new issue in a GitHub repository")
    public ApiResponse<IssueResponse> createIssue(
            @McpToolParam(description = "Repository owner", required = true) String owner,
            @McpToolParam(description = "Repository name", required = true) String repo,
            @McpToolParam(description = "Issue title", required = true) String title,
            @McpToolParam(description = "Issue body/description") String body,
            @McpToolParam(description = "Comma-separated list of assignee usernames") String assignees,
            @McpToolParam(description = "Comma-separated list of label names") String labels,
            @McpToolParam(description = "Milestone number") Long milestone) {
        
        List<String> assigneeList = assignees != null ? Arrays.asList(assignees.split(",")) : null;
        List<String> labelList = labels != null ? Arrays.asList(labels.split(",")) : null;
        
        CreateIssueRequest request = CreateIssueRequest.builder()
                .title(title)
                .body(body)
                .assignees(assigneeList)
                .labels(labelList)
                .milestone(milestone)
                .build();
        
        return issueService.createIssue(owner, repo, request);
    }

    @McpTool(name = "update_issue", description = "Update an existing issue in a GitHub repository")
    public ApiResponse<IssueResponse> updateIssue(
            @McpToolParam(description = "Repository owner", required = true) String owner,
            @McpToolParam(description = "Repository name", required = true) String repo,
            @McpToolParam(description = "Issue number", required = true) Long issueNumber,
            @McpToolParam(description = "New title") String title,
            @McpToolParam(description = "New body/description") String body,
            @McpToolParam(description = "Comma-separated list of label names") String labels,
            @McpToolParam(description = "Milestone number") Long milestone) {
        
        List<String> labelList = labels != null ? Arrays.asList(labels.split(",")) : null;
        
        UpdateIssueRequest request = UpdateIssueRequest.builder()
                .title(title)
                .body(body)
                .labels(labelList)
                .milestone(milestone)
                .build();
        
        return issueService.updateIssue(owner, repo, issueNumber, request);
    }

    @McpTool(name = "add_issue_comment", description = "Add a comment to an existing issue")
    public ApiResponse<IssueResponse> addIssueComment(
            @McpToolParam(description = "Repository owner", required = true) String owner,
            @McpToolParam(description = "Repository name", required = true) String repo,
            @McpToolParam(description = "Issue number", required = true) Long issueNumber,
            @McpToolParam(description = "Comment body", required = true) String body) {
        
        return issueService.addIssueComment(owner, repo, issueNumber, body);
    }

    @McpTool(name = "list_issues", description = "List issues in a GitHub repository")
    public ApiResponse<PagedResponse<IssueResponse>> listIssues(
            @McpToolParam(description = "Repository owner", required = true) String owner,
            @McpToolParam(description = "Repository name", required = true) String repo,
            @McpToolParam(description = "Issue state: open, closed, all") String state,
            @McpToolParam(description = "Comma-separated list of label names") String labels,
            @McpToolParam(description = "Filter by assignee username") String assignee,
            @McpToolParam(description = "Page number") Integer page,
            @McpToolParam(description = "Items per page (max 100)") Integer perPage) {
        
        return issueService.listIssues(owner, repo, state, labels, assignee, page, perPage);
    }

    @McpTool(name = "get_issue", description = "Get details of a specific issue")
    public ApiResponse<IssueResponse> getIssue(
            @McpToolParam(description = "Repository owner", required = true) String owner,
            @McpToolParam(description = "Repository name", required = true) String repo,
            @McpToolParam(description = "Issue number", required = true) Long issueNumber) {
        
        return issueService.getIssue(owner, repo, issueNumber);
    }

    @McpTool(name = "close_issue", description = "Close an issue")
    public ApiResponse<IssueResponse> closeIssue(
            @McpToolParam(description = "Repository owner", required = true) String owner,
            @McpToolParam(description = "Repository name", required = true) String repo,
            @McpToolParam(description = "Issue number", required = true) Long issueNumber) {
        
        return issueService.closeIssue(owner, repo, issueNumber);
    }

    @McpTool(name = "reopen_issue", description = "Reopen a closed issue")
    public ApiResponse<IssueResponse> reopenIssue(
            @McpToolParam(description = "Repository owner", required = true) String owner,
            @McpToolParam(description = "Repository name", required = true) String repo,
            @McpToolParam(description = "Issue number", required = true) Long issueNumber) {
        
        return issueService.reopenIssue(owner, repo, issueNumber);
    }

    @McpTool(name = "assign_issue", description = "Assign an issue to users")
    public ApiResponse<IssueResponse> assignIssue(
            @McpToolParam(description = "Repository owner", required = true) String owner,
            @McpToolParam(description = "Repository name", required = true) String repo,
            @McpToolParam(description = "Issue number", required = true) Long issueNumber,
            @McpToolParam(description = "Comma-separated list of usernames to assign", required = true) String assignees) {
        
        List<String> assigneeList = Arrays.asList(assignees.split(","));
        return issueService.assignIssue(owner, repo, issueNumber, assigneeList);
    }

    @McpTool(name = "unassign_issue", description = "Remove all assignees from an issue")
    public ApiResponse<IssueResponse> unassignIssue(
            @McpToolParam(description = "Repository owner", required = true) String owner,
            @McpToolParam(description = "Repository name", required = true) String repo,
            @McpToolParam(description = "Issue number", required = true) Long issueNumber) {
        
        return issueService.unassignIssue(owner, repo, issueNumber);
    }

    @McpTool(name = "add_issue_labels", description = "Add labels to an issue")
    public ApiResponse<IssueResponse> addIssueLabels(
            @McpToolParam(description = "Repository owner", required = true) String owner,
            @McpToolParam(description = "Repository name", required = true) String repo,
            @McpToolParam(description = "Issue number", required = true) Long issueNumber,
            @McpToolParam(description = "Comma-separated list of label names", required = true) String labels) {
        
        List<String> labelList = Arrays.asList(labels.split(","));
        return issueService.addIssueLabels(owner, repo, issueNumber, labelList);
    }

    @McpTool(name = "remove_issue_label", description = "Remove a label from an issue")
    public ApiResponse<IssueResponse> removeIssueLabel(
            @McpToolParam(description = "Repository owner", required = true) String owner,
            @McpToolParam(description = "Repository name", required = true) String repo,
            @McpToolParam(description = "Issue number", required = true) Long issueNumber,
            @McpToolParam(description = "Label name to remove", required = true) String label) {
        
        return issueService.removeIssueLabel(owner, repo, issueNumber, label);
    }
}
