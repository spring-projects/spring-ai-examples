package com.github.mcp.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowRunResponse {
    private Long id;
    private String name;
    private String nodeId;
    private Long repositoryId;
    private String headBranch;
    private String headSha;
    private String path;
    private String displayTitle;
    private Long runNumber;
    private String event;
    private String status;
    private String conclusion;
    private Long workflowId;
    private String url;
    private String htmlUrl;
    private String logsUrl;
    private String checkSuiteUrl;
    private String artifactsUrl;
    private String cancelUrl;
    private String rerunUrl;
    private String workflowUrl;
    private HeadCommit headCommit;
    private RepositoryResponse repository;
    private UserResponse triggeringActor;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant runStartedAt;
    private Integer jobsUrl;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HeadCommit {
        private String id;
        private String treeId;
        private String message;
        private Instant timestamp;
        private String author;
        private String committer;
    }
}
