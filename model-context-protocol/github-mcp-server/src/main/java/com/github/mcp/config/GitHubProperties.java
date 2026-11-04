package com.github.mcp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;

@Data
@Validated
@ConfigurationProperties(prefix = "github")
public class GitHubProperties {
    private String personalAccessToken;
    private String host = "github.com";
    private List<String> toolsets;
    private List<String> tools;
    private List<String> excludeTools;
    private List<String> features;
    private boolean dynamicToolsets = false;
    private boolean readOnly = false;
    private String logFile;
    private boolean enableCommandLogging = false;
    private boolean exportTranslations = false;
    private int contentWindowSize = 5000;
    private boolean lockdownMode = false;
    private boolean insiders = false;
    private Duration repoAccessCacheTtl = Duration.ofMinutes(5);
}
