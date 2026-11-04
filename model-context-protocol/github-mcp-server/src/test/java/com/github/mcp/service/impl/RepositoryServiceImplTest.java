package com.github.mcp.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mcp.client.GitHubRestClient;
import com.github.mcp.config.WebConfig;
import com.github.mcp.dto.common.ApiResponse;
import com.github.mcp.dto.common.PagedResponse;
import com.github.mcp.dto.response.BranchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryServiceImplTest {

    @Mock
    private GitHubRestClient gitHubClient;

    private RepositoryServiceImpl repositoryService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new WebConfig().objectMapper();
        repositoryService = new RepositoryServiceImpl(gitHubClient, objectMapper);
    }

    @Test
    void listBranchesShouldNormalizeOwnerAndRepoWhenRepoContainsFullName() {
        when(gitHubClient.getRawResponse(anyString(), anyMap()))
                .thenReturn(ResponseEntity.ok("""
                        [
                          {
                            "name": "main",
                            "protected": true,
                            "commit": {
                              "sha": "abc123",
                              "url": "https://api.github.com/repos/parthloglogn/free-shorts-app/commits/abc123"
                            }
                          }
                        ]
                        """));

        ApiResponse<PagedResponse<BranchResponse>> response = repositoryService.listBranches(
                "parthloglogn",
                "parthloglogn/free-shorts-app",
                1,
                30);

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass((Class) Map.class);
        verify(gitHubClient).getRawResponse(pathCaptor.capture(), paramsCaptor.capture());

        assertThat(pathCaptor.getValue()).isEqualTo("/repos/parthloglogn/free-shorts-app/branches");
        assertThat(paramsCaptor.getValue()).containsEntry("page", 1).containsEntry("per_page", 30);
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getItems()).hasSize(1);
        assertThat(response.getData().getItems().getFirst().getName()).isEqualTo("main");
        assertThat(response.getData().getItems().getFirst().getProtected_branch()).isTrue();
    }
}

