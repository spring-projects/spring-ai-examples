package com.github.mcp.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class RepositoryResponse {
    private Long id;
    private String name;
    private String fullName;
    private String description;
    private String url;
    private String htmlUrl;
    private String cloneUrl;
    private String sshUrl;
    private Boolean private_repo;
    private Boolean fork;
    
    @JsonProperty("private")
    public void setPrivate(Boolean private_repo) {
        this.private_repo = private_repo;
    }
    
    @JsonProperty("private")
    public Boolean getPrivate() {
        return private_repo;
    }
    
    private String defaultBranch;
    private Integer forksCount;
    private Integer stargazersCount;
    private Integer watchersCount;
    private Integer openIssuesCount;
    private String language;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant pushedAt;
    private Owner owner;
    private License license;
    private Boolean archived;
    private Boolean disabled;
    private String visibility;
    private List<String> topics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Owner {
        private String login;
        private Long id;
        private String avatarUrl;
        private String htmlUrl;
        private String type;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class License {
        private String key;
        private String name;
        private String spdxId;
        private String url;
    }
    
}
