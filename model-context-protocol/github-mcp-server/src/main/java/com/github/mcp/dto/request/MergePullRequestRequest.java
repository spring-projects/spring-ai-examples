package com.github.mcp.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MergePullRequestRequest {
    private String commitTitle;
    private String commitMessage;
    private String sha;
    private String mergeMethod;
}
