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
public class IssueResponse {
    private Long id;
    private Long number;
    private String title;
    private String body;
    private String state;
    private String stateReason;
    private String htmlUrl;
    private UserResponse user;
    private List<UserResponse> assignees;
    private List<LabelResponse> labels;
    private MilestoneResponse milestone;
    private Boolean locked;
    private Integer comments;
    private PullRequestReference pullRequest;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant closedAt;
    private UserResponse closedBy;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PullRequestReference {
        private String url;
        private String htmlUrl;
    }
}
