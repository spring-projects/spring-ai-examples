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
public class MilestoneResponse {
    private Long id;
    private Long number;
    private String nodeId;
    private String url;
    private String htmlUrl;
    private String labelsUrl;
    private String title;
    private String description;
    private String state;
    private Integer openIssues;
    private Integer closedIssues;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant dueOn;
    private Instant closedAt;
    private UserResponse creator;
}
