package com.github.mcp.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mcp.config.GitHubProperties;
import com.github.mcp.config.WebConfig;
import com.github.mcp.dto.response.PullRequestResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GitHubRestClientTest {

    @Test
    void getShouldDeserializePullRequestResponseWithNestedRepositoryTopics() {
        WebConfig webConfig = new WebConfig();
        ObjectMapper objectMapper = webConfig.objectMapper();
        RestTemplate restTemplate = webConfig.restTemplate();

        GitHubProperties properties = new GitHubProperties();
        properties.setHost("github.com");
        properties.setPersonalAccessToken("test-token");

        GitHubRestClient client = new GitHubRestClient(restTemplate, properties, objectMapper);
        client.init();

        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo("https://api.github.com/repos/octocat/hello-world/pulls/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "id": 101,
                          "number": 1,
                          "title": "Add feature",
                          "state": "open",
                          "html_url": "https://github.com/octocat/hello-world/pull/1",
                          "head": {
                            "label": "octocat:feature",
                            "ref": "feature",
                            "sha": "abc123",
                            "repo": {
                              "id": 200,
                              "name": "hello-world",
                              "full_name": "octocat/hello-world",
                              "private": false,
                              "default_branch": "main",
                              "topics": ["java", "spring"]
                            },
                            "user": {
                              "login": "octocat",
                              "id": 1
                            }
                          },
                          "base": {
                            "label": "octocat:main",
                            "ref": "main",
                            "sha": "def456",
                            "repo": {
                              "id": 200,
                              "name": "hello-world",
                              "full_name": "octocat/hello-world",
                              "private": false,
                              "default_branch": "main",
                              "topics": ["java", "spring"]
                            },
                            "user": {
                              "login": "octocat",
                              "id": 1
                            }
                          },
                          "created_at": "2026-03-30T10:00:00Z",
                          "updated_at": "2026-03-30T11:00:00Z"
                        }
                        """, MediaType.APPLICATION_JSON));

        PullRequestResponse response = client.get("/repos/octocat/hello-world/pulls/1", PullRequestResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getNumber()).isEqualTo(1L);
        assertThat(response.getHead()).isNotNull();
        assertThat(response.getHead().getRepo()).isNotNull();
        assertThat(response.getHead().getRepo().getFullName()).isEqualTo("octocat/hello-world");
        assertThat(response.getHead().getRepo().getTopics()).containsExactly("java", "spring");
        assertThat(response.getBase().getRepo().getTopics()).containsExactly("java", "spring");

        server.verify();
    }
}

