package com.github.mcp.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationResponse {
    private String id;
    private String repositoryId;
    private String unread;
    private String reason;
    private Instant updatedAt;
    private Instant lastReadAt;
    private String url;
    private Subject subject;
    private RepositoryResponse repository;
    private String subscriptionUrl;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Subject {
        private String title;
        private String url;
        private String latestCommentUrl;
        private String type;
    }
}
