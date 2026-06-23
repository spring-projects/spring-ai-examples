package com.github.mcp.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GistResponse {
    private String url;
    private String forksUrl;
    private String commitsUrl;
    private String id;
    private String nodeId;
    private String gitPullUrl;
    private String gitPushUrl;
    private String htmlUrl;
    private Map<String, GistFile> files;
    private Boolean public_gist;
    private String createdAt;
    private String updatedAt;
    private String description;
    private Integer comments;
    private UserResponse user;
    private String commentsUrl;
    private String owner;
    private Boolean truncated;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GistFile {
        private String filename;
        private String type;
        private String language;
        private String rawUrl;
        private Integer size;
        private Boolean truncated;
        private String content;
    }
}
