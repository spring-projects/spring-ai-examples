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
public class CreatePullRequestRequest {
    private String title;
    private String body;
    private String head;
    private String base;
    private Boolean draft;
    private Boolean maintainerCanModify;
}
