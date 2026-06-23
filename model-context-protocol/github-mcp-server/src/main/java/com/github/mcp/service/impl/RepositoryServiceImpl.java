package com.github.mcp.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mcp.client.GitHubRestClient;
import com.github.mcp.dto.common.ApiResponse;
import com.github.mcp.dto.common.PagedResponse;
import com.github.mcp.dto.request.CreateBranchRequest;
import com.github.mcp.dto.request.CreateFileRequest;
import com.github.mcp.dto.request.CreateRepositoryRequest;
import com.github.mcp.dto.response.*;
import com.github.mcp.exception.GitHubMcpException;
import com.github.mcp.service.interfaces.RepositoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepositoryServiceImpl implements RepositoryService {

    private final GitHubRestClient gitHubClient;
    private final ObjectMapper objectMapper;

    @Override
    public ApiResponse<RepositoryResponse> createRepository(CreateRepositoryRequest request) {
        try {
            String path = "/user/repos";
            RepositoryResponse response = gitHubClient.post(path, request, RepositoryResponse.class);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("Error creating repository: {}", e.getMessage());
            return ApiResponse.error("CREATE_ERROR", "Failed to create repository: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<RepositoryResponse> forkRepository(String owner, String repo, String organization) {
        try {
            String path = String.format("/repos/%s/%s/forks", owner, repo);
            Map<String, Object> body = new HashMap<>();
            if (organization != null) {
                body.put("organization", organization);
            }
            RepositoryResponse response = gitHubClient.post(path, body, RepositoryResponse.class);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("Error forking repository: {}", e.getMessage());
            return ApiResponse.error("FORK_ERROR", "Failed to fork repository: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<RepositoryResponse> getRepository(String owner, String repo) {
        try {
            String path = String.format("/repos/%s/%s", owner, repo);
            RepositoryResponse response = gitHubClient.get(path, RepositoryResponse.class);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("Error getting repository: {}", e.getMessage());
            return ApiResponse.error("GET_ERROR", "Failed to get repository: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<PagedResponse<CommitResponse>> listCommits(String owner, String repo, String sha, String path, Integer page, Integer perPage) {
        try {
            String url = String.format("/repos/%s/%s/commits", owner, repo);
            Map<String, Object> params = new HashMap<>();
            if (sha != null) params.put("sha", sha);
            if (path != null) params.put("path", path);
            params.put("page", page != null ? page : 1);
            params.put("per_page", perPage != null ? perPage : 30);
            
            ResponseEntity<String> rawResponse = gitHubClient.getRawResponse(url, params);
            List<CommitResponse> commits = objectMapper.readerForListOf(CommitResponse.class)
                    .readValue(rawResponse.getBody());
            
            return ApiResponse.success(PagedResponse.of(commits, page != null ? page : 1, perPage != null ? perPage : 30, commits.size()));
        } catch (Exception e) {
            log.error("Error listing commits: {}", e.getMessage());
            return ApiResponse.error("LIST_ERROR", "Failed to list commits: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<CommitResponse> getCommit(String owner, String repo, String ref) {
        try {
            String path = String.format("/repos/%s/%s/commits/%s", owner, repo, ref);
            CommitResponse response = gitHubClient.get(path, CommitResponse.class);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("Error getting commit: {}", e.getMessage());
            return ApiResponse.error("GET_ERROR", "Failed to get commit: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<FileContentResponse> getFileContents(String owner, String repo, String path, String ref) {
        try {
            String url = String.format("/repos/%s/%s/contents/%s", owner, repo, path);
            Map<String, Object> params = new HashMap<>();
            if (ref != null) params.put("ref", ref);
            
            FileContentResponse response = gitHubClient.get(url, params, FileContentResponse.class);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("Error getting file contents: {}", e.getMessage());
            return ApiResponse.error("GET_ERROR", "Failed to get file contents: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<Map<String, Object>> createOrUpdateFile(String owner, String repo, String path, CreateFileRequest request) {
        try {
            String url = String.format("/repos/%s/%s/contents/%s", owner, repo, path);
            Map<String, Object> response = gitHubClient.put(url, request, Map.class);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("Error creating/updating file: {}", e.getMessage());
            return ApiResponse.error("UPDATE_ERROR", "Failed to create/update file: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<Void> deleteFile(String owner, String repo, String path, String message, String sha, String branch) {
        try {
            String url = String.format("/repos/%s/%s/contents/%s", owner, repo, path);
            Map<String, Object> body = new HashMap<>();
            body.put("message", message);
            body.put("sha", sha);
            if (branch != null) body.put("branch", branch);
            
            gitHubClient.delete(url);
            return ApiResponse.success(null);
        } catch (Exception e) {
            log.error("Error deleting file: {}", e.getMessage());
            return ApiResponse.error("DELETE_ERROR", "Failed to delete file: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<BranchResponse> createBranch(String owner, String repo, CreateBranchRequest request) {
        try {
            String path = String.format("/repos/%s/%s/git/refs", owner, repo);
            Map<String, Object> body = new HashMap<>();
            body.put("ref", "refs/heads/" + request.getRef());
            body.put("sha", request.getSha());
            
            BranchResponse response = gitHubClient.post(path, body, BranchResponse.class);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("Error creating branch: {}", e.getMessage());
            return ApiResponse.error("CREATE_ERROR", "Failed to create branch: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<PagedResponse<BranchResponse>> listBranches(String owner, String repo, Integer page, Integer perPage) {
        try {
            RepositoryCoordinates coordinates = normalizeRepositoryCoordinates(owner, repo);
            String url = String.format("/repos/%s/%s/branches", coordinates.owner(), coordinates.repo());
            Map<String, Object> params = new HashMap<>();
            params.put("page", page != null ? page : 1);
            params.put("per_page", perPage != null ? perPage : 30);
            
            ResponseEntity<String> rawResponse = gitHubClient.getRawResponse(url, params);
            List<BranchResponse> branches = objectMapper.readerForListOf(BranchResponse.class)
                    .readValue(rawResponse.getBody());
            
            return ApiResponse.success(PagedResponse.of(branches, page != null ? page : 1, perPage != null ? perPage : 30, branches.size()));
        } catch (Exception e) {
            log.error("Error listing branches: {}", e.getMessage());
            return ApiResponse.error("LIST_ERROR", "Failed to list branches: " + e.getMessage());
        }
    }

    private RepositoryCoordinates normalizeRepositoryCoordinates(String owner, String repo) {
        if (!StringUtils.hasText(repo)) {
            throw new GitHubMcpException("INVALID_REPOSITORY", "Repository name is required", 400);
        }

        String trimmedRepo = repo.trim();
        if (trimmedRepo.contains("/")) {
            String[] parts = trimmedRepo.split("/");
            if (parts.length == 2 && StringUtils.hasText(parts[0]) && StringUtils.hasText(parts[1])) {
                if (!StringUtils.hasText(owner) || owner.trim().equals(parts[0])) {
                    return new RepositoryCoordinates(parts[0].trim(), parts[1].trim());
                }
            }
            throw new GitHubMcpException(
                    "INVALID_REPOSITORY",
                    "Repository must be provided either as repo or matching owner/repo pair",
                    400);
        }

        if (!StringUtils.hasText(owner)) {
            throw new GitHubMcpException("INVALID_REPOSITORY", "Repository owner is required", 400);
        }

        return new RepositoryCoordinates(owner.trim(), trimmedRepo);
    }

    private record RepositoryCoordinates(String owner, String repo) {
    }

    @Override
    public ApiResponse<Map<String, Object>> mergeBranch(String owner, String repo, String base, String head, String commitMessage) {
        try {
            String path = String.format("/repos/%s/%s/merges", owner, repo);
            Map<String, Object> body = new HashMap<>();
            body.put("base", base);
            body.put("head", head);
            if (commitMessage != null) body.put("commit_message", commitMessage);
            
            Map<String, Object> response = gitHubClient.post(path, body, Map.class);
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("Error merging branch: {}", e.getMessage());
            return ApiResponse.error("MERGE_ERROR", "Failed to merge branch: " + e.getMessage());
        }
    }
}
