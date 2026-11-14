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
public class CommitResponse {
    private String sha;
    private String nodeId;
    private String htmlUrl;
    private String url;
    private CommitDetail commit;
    private UserResponse author;
    private UserResponse committer;
    private List<ParentCommit> parents;
    private Stats stats;
    private List<FileChange> files;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CommitDetail {
        private String message;
        private CommitAuthor author;
        private CommitAuthor committer;
        private Tree tree;
        private Integer commentCount;
        private Verification verification;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CommitAuthor {
        private String name;
        private String email;
        private Instant date;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Tree {
        private String sha;
        private String url;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Verification {
        private Boolean verified;
        private String reason;
        private String signature;
        private String payload;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ParentCommit {
        private String sha;
        private String url;
        private String htmlUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Stats {
        private Integer additions;
        private Integer deletions;
        private Integer total;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FileChange {
        private String sha;
        private String filename;
        private String status;
        private Integer additions;
        private Integer deletions;
        private Integer changes;
        private String blobUrl;
        private String rawUrl;
        private String contentsUrl;
        private String patch;
        private String previousFilename;
    }
}
