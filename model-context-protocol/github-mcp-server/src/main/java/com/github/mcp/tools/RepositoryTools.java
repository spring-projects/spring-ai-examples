package com.github.mcp.tools;

import com.github.mcp.dto.common.ApiResponse;
import com.github.mcp.dto.common.PagedResponse;
import com.github.mcp.dto.request.CreateBranchRequest;
import com.github.mcp.dto.request.CreateFileRequest;
import com.github.mcp.dto.request.CreateRepositoryRequest;
import com.github.mcp.dto.response.*;
import com.github.mcp.service.interfaces.RepositoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RepositoryTools {

    private final RepositoryService repositoryService;

    @McpTool(name = "create_repository", description = "Create a new GitHub repository for the authenticated user")
    public ApiResponse<RepositoryResponse> createRepository(
            @McpToolParam(description = "Repository name", required = true) String name,
            @McpToolParam(description = "Repository description") String description,
            @McpToolParam(description = "Whether the repository should be private") Boolean isPrivate,
            @McpToolParam(description = "Whether to initialize with a README") Boolean autoInit) {
        
        CreateRepositoryRequest request = CreateRepositoryRequest.builder()
                .name(name)
                .description(description)
                .private_repo(isPrivate != null ? isPrivate : false)
                .autoInit(autoInit != null ? autoInit : false)
                .hasIssues(true)
                .hasWiki(true)
                .build();
        
        return repositoryService.createRepository(request);
    }

    @McpTool(name = "fork_repository", description = "Fork a GitHub repository to your account or specified organization")
    public ApiResponse<RepositoryResponse> forkRepository(
            @McpToolParam(description = "Repository owner", required = true) String owner,
            @McpToolParam(description = "Repository name", required = true) String repo,
            @McpToolParam(description = "Organization to fork to (optional, defaults to your account)") String organization) {
        
        return repositoryService.forkRepository(owner, repo, organization);
    }

    @McpTool(name = "get_repository", description = "Get details of a specific GitHub repository")
    public ApiResponse<RepositoryResponse> getRepository(
            @McpToolParam(description = "Repository owner", required = true) String owner,
            @McpToolParam(description = "Repository name", required = true) String repo) {
        
        return repositoryService.getRepository(owner, repo);
    }

    @McpTool(name = "list_commits", description = "List commits in a GitHub repository")
    public ApiResponse<PagedResponse<CommitResponse>> listCommits(
            @McpToolParam(description = "Repository owner", required = true) String owner,
            @McpToolParam(description = "Repository name", required = true) String repo,
            @McpToolParam(description = "Branch name or commit SHA") String sha,
            @McpToolParam(description = "Path to filter commits by file") String path,
            @McpToolParam(description = "Page number") Integer page,
            @McpToolParam(description = "Items per page (max 100)") Integer perPage) {
        
        return repositoryService.listCommits(owner, repo, sha, path, page, perPage);
    }

    @McpTool(name = "get_commit", description = "Get details of a specific commit")
    public ApiResponse<CommitResponse> getCommit(
            @McpToolParam(description = "Repository owner", required = true) String owner,
            @McpToolParam(description = "Repository name", required = true) String repo,
            @McpToolParam(description = "Commit SHA or reference", required = true) String ref) {
        
        return repositoryService.getCommit(owner, repo, ref);
    }

    @McpTool(name = "get_file_contents", description = "Get the contents of a file in a GitHub repository")
    public ApiResponse<FileContentResponse> getFileContents(
            @McpToolParam(description = "Repository owner", required = true) String owner,
            @McpToolParam(description = "Repository name", required = true) String repo,
            @McpToolParam(description = "Path to the file", required = true) String path,
            @McpToolParam(description = "Branch name, tag, or commit SHA") String ref) {
        
        return repositoryService.getFileContents(owner, repo, path, ref);
    }

    @McpTool(name = "create_or_update_file", description = "Create or update a file in a GitHub repository")
    public ApiResponse<Map<String, Object>> createOrUpdateFile(
            @McpToolParam(description = "Repository owner", required = true) String owner,
            @McpToolParam(description = "Repository name", required = true) String repo,
            @McpToolParam(description = "Path to the file", required = true) String path,
            @McpToolParam(description = "Commit message", required = true) String message,
            @McpToolParam(description = "File content (plain text)", required = true) String content,
            @McpToolParam(description = "Branch name") String branch,
            @McpToolParam(description = "SHA of the file being replaced (required for updates)") String sha) {
        
        String encodedContent = Base64.getEncoder().encodeToString(content.getBytes());
        
        CreateFileRequest request = CreateFileRequest.builder()
                .message(message)
                .content(encodedContent)
                .sha(sha)
                .branch(branch)
                .build();
        
        return repositoryService.createOrUpdateFile(owner, repo, path, request);
    }

    @McpTool(name = "delete_file", description = "Delete a file from a GitHub repository")
    public ApiResponse<Void> deleteFile(
            @McpToolParam(description = "Repository owner", required = true) String owner,
            @McpToolParam(description = "Repository name", required = true) String repo,
            @McpToolParam(description = "Path to the file", required = true) String path,
            @McpToolParam(description = "Commit message", required = true) String message,
            @McpToolParam(description = "SHA of the file to delete", required = true) String sha,
            @McpToolParam(description = "Branch name") String branch) {
        
        return repositoryService.deleteFile(owner, repo, path, message, sha, branch);
    }

    @McpTool(name = "create_branch", description = "Create a new branch in a GitHub repository")
    public ApiResponse<BranchResponse> createBranch(
            @McpToolParam(description = "Repository owner", required = true) String owner,
            @McpToolParam(description = "Repository name", required = true) String repo,
            @McpToolParam(description = "Name for the new branch", required = true) String branch,
            @McpToolParam(description = "SHA of the commit to branch from", required = true) String fromSha) {
        
        CreateBranchRequest request = CreateBranchRequest.builder()
                .ref(branch)
                .sha(fromSha)
                .build();
        
        return repositoryService.createBranch(owner, repo, request);
    }

    @McpTool(name = "list_branches", description = "List branches in a GitHub repository")
    public ApiResponse<PagedResponse<BranchResponse>> listBranches(
            @McpToolParam(description = "Repository owner", required = true) String owner,
            @McpToolParam(description = "Repository name", required = true) String repo,
            @McpToolParam(description = "Page number") Integer page,
            @McpToolParam(description = "Items per page (max 100)") Integer perPage) {
        
        return repositoryService.listBranches(owner, repo, page, perPage);
    }

    @McpTool(name = "merge_branch", description = "Merge one branch into another in a GitHub repository")
    public ApiResponse<Map<String, Object>> mergeBranch(
            @McpToolParam(description = "Repository owner", required = true) String owner,
            @McpToolParam(description = "Repository name", required = true) String repo,
            @McpToolParam(description = "Base branch to merge into", required = true) String base,
            @McpToolParam(description = "Head branch to merge from", required = true) String head,
            @McpToolParam(description = "Commit message for the merge") String commitMessage) {
        
        return repositoryService.mergeBranch(owner, repo, base, head, commitMessage);
    }
}
