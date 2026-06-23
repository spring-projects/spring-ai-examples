package com.github.mcp.service.interfaces;

import com.github.mcp.dto.common.ApiResponse;
import com.github.mcp.dto.common.PagedResponse;
import com.github.mcp.dto.request.CreateBranchRequest;
import com.github.mcp.dto.request.CreateFileRequest;
import com.github.mcp.dto.request.CreateRepositoryRequest;
import com.github.mcp.dto.response.BranchResponse;
import com.github.mcp.dto.response.CommitResponse;
import com.github.mcp.dto.response.FileContentResponse;
import com.github.mcp.dto.response.RepositoryResponse;

import java.util.Map;

public interface RepositoryService {
    ApiResponse<RepositoryResponse> createRepository(CreateRepositoryRequest request);

    ApiResponse<RepositoryResponse> forkRepository(String owner, String repo, String organization);

    ApiResponse<RepositoryResponse> getRepository(String owner, String repo);

    ApiResponse<PagedResponse<CommitResponse>> listCommits(String owner, String repo, String sha, String path, Integer page, Integer perPage);

    ApiResponse<CommitResponse> getCommit(String owner, String repo, String ref);

    ApiResponse<FileContentResponse> getFileContents(String owner, String repo, String path, String ref);

    ApiResponse<Map<String, Object>> createOrUpdateFile(String owner, String repo, String path, CreateFileRequest request);

    ApiResponse<Void> deleteFile(String owner, String repo, String path, String message, String sha, String branch);

    ApiResponse<BranchResponse> createBranch(String owner, String repo, CreateBranchRequest request);

    ApiResponse<PagedResponse<BranchResponse>> listBranches(String owner, String repo, Integer page, Integer perPage);

    ApiResponse<Map<String, Object>> mergeBranch(String owner, String repo, String base, String head, String commitMessage);
}
