package com.github.mcp.exception;

import lombok.Getter;

@Getter
public class GitHubMcpException extends RuntimeException {
    private final String code;
    private final int statusCode;

    public GitHubMcpException(String message) {
        super(message);
        this.code = "GITHUB_ERROR";
        this.statusCode = 500;
    }

    public GitHubMcpException(String code, String message) {
        super(message);
        this.code = code;
        this.statusCode = 500;
    }

    public GitHubMcpException(String code, String message, int statusCode) {
        super(message);
        this.code = code;
        this.statusCode = statusCode;
    }

    public GitHubMcpException(String message, Throwable cause) {
        super(message, cause);
        this.code = "GITHUB_ERROR";
        this.statusCode = 500;
    }
}
