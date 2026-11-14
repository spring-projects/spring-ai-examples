package com.github.mcp.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePullRequestRequest {
    private String title;
    private String body;
    private String state;
    private String base;
    private Boolean maintainerCanModify;
}
