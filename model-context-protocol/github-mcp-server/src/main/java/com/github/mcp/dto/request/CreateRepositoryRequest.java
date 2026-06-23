package com.github.mcp.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRepositoryRequest {
    private String name;
    private String description;
    private String homepage;
    private Boolean private_repo;
    private Boolean hasIssues;
    private Boolean hasProjects;
    private Boolean hasWiki;
    private Boolean hasDownloads;
    private Boolean isTemplate;
    private String teamId;
    private Boolean autoInit;
    private String gitignoreTemplate;
    private String licenseTemplate;
    private Boolean allowSquashMerge;
    private Boolean allowMergeCommit;
    private Boolean allowRebaseMerge;
    private Boolean deleteBranchOnMerge;
}
