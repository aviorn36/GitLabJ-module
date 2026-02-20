package com.aviorn36.GitSync.GitSynchronization.controllers;

import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.webhook.Event;
import org.gitlab4j.api.webhook.WebHookManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class GitSyncControllerV2 {

    @Value("${git.server.token}")
    private String token;
    @Value("${git.server.branch}")
    private String branch;
    @Value("${git.server.url}")
    private String gitUrl;
    @Value("${git.server.repo}")
    private String repositoryName;
    @Value("${application.flow.configRootPath}")
    private String dynamicConfigRootPath;

    @Autowired
    private WebHookManager webHookManager;

    @PostMapping("/reloadDynamicFlowConfigurationV2")
    public void reloadDynamicFlowConfiguration(@RequestBody Event event) throws GitLabApiException, IOException {
        System.out.println("Reloading configuration initiated...");
        webHookManager.handleEvent(event);
    }
}
