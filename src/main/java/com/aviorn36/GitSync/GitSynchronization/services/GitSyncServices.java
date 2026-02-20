package com.aviorn36.GitSync.GitSynchronization.services;

import com.aviorn36.GitSync.GitSynchronization.utilities.GitSyncUtilities;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.RepositoryApi;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.TreeItem;
import org.gitlab4j.api.webhook.EventCommit;
import org.gitlab4j.api.webhook.PushEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class GitSyncServices {

    @Autowired
    private GitLabApi gitLabApi;

    @Value("${git.server.repo}")
    private String repositoryName;

    @Value("${application.flow.configRootPath}")
    private String dynamicConfigRootPath;

    @Value("${git.server.branch}")
    private String branch;

    /*
    To be used when application restarts. This reloads files and folders present in gitLab:repositoryName inside application:dynamicConfigRootPath
    Note: Recursively delete dynamicConfigRootPath before calling this or take backup automatically
    */
    public void loadAllConfiguration() throws GitLabApiException {

        // get project from gitLab
        Project dynamicConfigurationProject = GitSyncUtilities.getGitProject(gitLabApi, repositoryName);

        // create directory for flow configuration in application root path
        String dynamicConfigurationRootPath = "./".concat(dynamicConfigRootPath);
        GitSyncUtilities.createDirectory(dynamicConfigurationRootPath);

        // get tree structure of files present in repo and create directories structure inside dynamicConfigurationRootPath
        List<TreeItem> filesTreeItems = GitSyncUtilities.getAllFilesAndCreateDirectory(dynamicConfigurationProject, new RepositoryApi(gitLabApi), branch, dynamicConfigurationRootPath);

        // create files inside dynamicConfigurationRootPath directories in application
        GitSyncUtilities.createFilesFromTreeItems(gitLabApi, dynamicConfigurationProject, branch, filesTreeItems, dynamicConfigurationRootPath);
    }

    public void loadCommittedChanges(PushEvent pushEvent) throws GitLabApiException {
        //System.out.println(pushEvent);
        Project dynamicConfigurationProject = GitSyncUtilities.getGitProject(gitLabApi, repositoryName);
        String dynamicConfigurationRootPath = "./".concat(dynamicConfigRootPath);
        pushEvent.getCommits().stream().forEach(
                eventCommit -> {
                    System.out.println("Processing commit :" + eventCommit.getId());
                    processRemovedItems(dynamicConfigurationRootPath, eventCommit);
                    processAddedItems(dynamicConfigurationProject, dynamicConfigurationRootPath, eventCommit);
                    processModifiedItems(dynamicConfigurationProject, dynamicConfigurationRootPath, eventCommit);
                }
        );

    }

    private void processModifiedItems(Project dynamicConfigurationProject, String dynamicConfigurationRootPath, EventCommit eventCommit) {
        eventCommit.getModified().stream().forEach(modifiedItem -> {
            try {
                GitSyncUtilities.processModifiedItem(gitLabApi, dynamicConfigurationProject, branch, dynamicConfigurationRootPath, modifiedItem);
            } catch (GitLabApiException e) {
                //throw new RuntimeException(e);
            }
        });
    }

    private void processAddedItems(Project dynamicConfigurationProject, String dynamicConfigurationRootPath, EventCommit eventCommit) {
        eventCommit.getAdded().stream().forEach(addedItem -> {
            try {
                String addedItemPath = addedItem.substring(0,addedItem.lastIndexOf("/"));
                //String addedItemName = addedItem.substring(addedItem.lastIndexOf("/")+1);
                GitSyncUtilities.processAddedItem(gitLabApi, dynamicConfigurationProject, branch, dynamicConfigurationRootPath, addedItemPath, addedItem);
            } catch (GitLabApiException e) {
                //throw new RuntimeException(e);
            }
        });
    }

    private void processRemovedItems(String dynamicConfigurationRootPath, EventCommit eventCommit) {
        eventCommit.getRemoved().stream().forEach(removedItem -> {
            try {
                GitSyncUtilities.processRemovedItem(dynamicConfigurationRootPath.concat("/").concat(removedItem));
                //String removedItemPath = removedItem.substring(0,removedItem.lastIndexOf("/"));
                //System.out.println("removedItemPath :" +removedItemPath);

            } catch (IOException e) {
                //throw new RuntimeException(e);
            }
        });
    }
}
