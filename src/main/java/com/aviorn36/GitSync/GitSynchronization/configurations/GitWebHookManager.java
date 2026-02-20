package com.aviorn36.GitSync.GitSynchronization.configurations;

import org.gitlab4j.api.webhook.WebHookManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
@DependsOn(value = "WebHookEventsImplementation")
public class GitWebHookManager {
    @Value("${application.flow.hookSecretToken}")
    private String secretToken;
    @Autowired
    WebHookEventsImplementation webHookEventsImplementation;

    @Bean
    public WebHookManager getWebHookManagerInstance(){
        //WebHookEventsImplementation webHookEventsImplementation = new WebHookEventsImplementation();
        WebHookManager webHookManager = new WebHookManager(secretToken);
        webHookManager.addListener(webHookEventsImplementation);
        return webHookManager;
    }

}
