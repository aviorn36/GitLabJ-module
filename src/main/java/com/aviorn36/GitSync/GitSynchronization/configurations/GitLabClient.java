package com.aviorn36.GitSync.GitSynchronization.configurations;

import org.gitlab4j.api.GitLabApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GitLabClient {

    @Value("${git.server.token}")
    private String token;

    @Value("${git.server.url}")
    private String gitUrl;

    @Bean
    public GitLabApi getGitLabClient() {
        return new GitLabApi(gitUrl, token);
    }

}
