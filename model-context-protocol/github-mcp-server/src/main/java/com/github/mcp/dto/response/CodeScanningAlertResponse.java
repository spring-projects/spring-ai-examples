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
public class CodeScanningAlertResponse {
    private Long number;
    private String nodeId;
    private String state;
    private String dismissalReason;
    private String dismissedAt;
    private String dismissedBy;
    private String rule;
    private String tool;
    private MostRecentInstance mostRecentInstance;
    private String instancesUrl;
    private String url;
    private String htmlUrl;
    private String fixedAt;
    private String fixedBy;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MostRecentInstance {
        private String ref;
        private String analysisKey;
        private String environment;
        private String state;
        private String commitSha;
        private String message;
        private Location location;
        private String htmlUrl;
        private String classifications;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Location {
        private String path;
        private Integer startLine;
        private Integer endLine;
        private Integer startColumn;
        private Integer endColumn;
    }
}
