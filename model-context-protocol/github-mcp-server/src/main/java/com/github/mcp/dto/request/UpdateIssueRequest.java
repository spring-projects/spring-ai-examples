package com.github.mcp.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateIssueRequest {
    private String title;
    private String body;
    private String state;
    private String stateReason;
    private String assignee;
    private List<String> assignees;
    private List<String> labels;
    private Long milestone;
}
