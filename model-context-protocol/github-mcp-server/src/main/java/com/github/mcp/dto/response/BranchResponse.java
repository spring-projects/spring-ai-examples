package com.github.mcp.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BranchResponse {
    private String name;
    private CommitRef commit;
    private Boolean protected_branch;
    private Protection protection;
    private String protectionUrl;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public void setProtected(Boolean protected_branch) {
        this.protected_branch = protected_branch;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CommitRef {
        private String sha;
        private String url;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Protection {
        private Boolean enabled;
        private RequiredStatusChecks requiredStatusChecks;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RequiredStatusChecks {
        private String enforcementLevel;
        private java.util.List<String> contexts;
        private Integer contextsUrl;
    }
}
