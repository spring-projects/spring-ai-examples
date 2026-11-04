package com.github.mcp.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PullRequestResponse {
    private Long id;
    private Long number;
    private String title;
    private String body;
    private String state;
    private String htmlUrl;
    private String diffUrl;
    private String patchUrl;
    private UserResponse user;
    private List<UserResponse> assignees;
    private List<LabelResponse> labels;
    private MilestoneResponse milestone;
    private Boolean locked;
    private Boolean draft;
    private String mergeCommitSha;
    private UserResponse mergedBy;
    private Instant mergedAt;
    private String mergeStateStatus;
    private Boolean mergeable;
    private Boolean rebaseable;
    private String mergeableState;
    private Integer comments;
    private Integer reviewComments;
    private Integer commits;
    private Integer additions;
    private Integer deletions;
    private Integer changedFiles;
    private BranchReference head;
    private BranchReference base;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant closedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BranchReference {
        private String label;
        private String ref;
        private String sha;
        private RepositoryResponse repo;
        private UserResponse user;
    }
}
