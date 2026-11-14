package com.github.mcp.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGistRequest {
    private String description;
    private Boolean public_gist;
    private Map<String, GistFile> files;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GistFile {
        private String content;
    }
}
