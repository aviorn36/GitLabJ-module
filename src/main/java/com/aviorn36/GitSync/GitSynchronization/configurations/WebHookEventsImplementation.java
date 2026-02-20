package com.aviorn36.GitSync.GitSynchronization.configurations;

import com.aviorn36.GitSync.GitSynchronization.services.GitSyncServices;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.webhook.PushEvent;
import org.gitlab4j.api.webhook.WebHookListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "WebHookEventsImplementation")
public class WebHookEventsImplementation implements WebHookListener {

    @Autowired
    private GitSyncServices gitSyncServices;

    @Override
    public void onPushEvent(PushEvent pushEvent) {
        System.out.println("Push Event triggered...");
        try {
            gitSyncServices.loadCommittedChanges(pushEvent);
        } catch (GitLabApiException e) {
            throw new RuntimeException(e);
        }
    }

}
