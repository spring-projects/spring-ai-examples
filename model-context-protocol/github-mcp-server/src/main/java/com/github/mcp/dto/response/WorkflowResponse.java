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
public class WorkflowResponse {
    private Long id;
    private String nodeId;
    private String name;
    private String path;
    private String state;
    private Long repositoryId;
    private String url;
    private String htmlUrl;
    private String badgeUrl;
    private Instant createdAt;
    private Instant updatedAt;
}
