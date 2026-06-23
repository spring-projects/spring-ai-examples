package com.github.mcp.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFileRequest {
    private String message;
    private String content;
    private String sha;
    private String branch;
    private Committer committer;
    private Committer author;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Committer {
        private String name;
        private String email;
        private String date;
    }
}
