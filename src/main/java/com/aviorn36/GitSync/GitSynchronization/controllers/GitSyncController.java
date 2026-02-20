package com.aviorn36.GitSync.GitSynchronization.controllers;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.RepositoryApi;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.RepositoryFile;
import org.gitlab4j.api.models.TreeItem;
import org.gitlab4j.api.webhook.Event;
import org.gitlab4j.api.webhook.WebHookManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class GitSyncController {

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

    @PostMapping("/triggerGit")
    public String testService(){
        System.out.println("Triggered");
        return "triggered";
    }

    @PostMapping("/reloadDynamicFlowConfiguration")
    public void reloadDynamicFlowConfiguration(@RequestBody Event event) throws GitLabApiException, IOException {
        System.out.println("Reloading configuration initiated...");
        System.out.println(event);
        webHookManager.handleEvent(event);
        // Create a GitLabApi instance to communicate with your GitLab server
        GitLabApi gitLabApi = new GitLabApi(gitUrl, token);

        // Get the list of projects your account has access to
        List<Project> projects = gitLabApi.getProjectApi().getProjects(repositoryName);

        Project adapterConfiguration= projects.stream().filter( project -> project.getName()
                                                 .equalsIgnoreCase(repositoryName)).findFirst().get();

        RepositoryApi repo = new RepositoryApi(gitLabApi);
        // Branch developBranch = repo.getBranch(adapterConfiguration, "develop");
        // List<Commit> commitsApi = new CommitsApi(gitLabApi).getCommits(adapterConfiguration);

        // create root directory for flow configuration
        File rootDirectory = new File("./".concat(dynamicConfigRootPath));
        String rootDirectoryPath = rootDirectory.getCanonicalPath();
        if(rootDirectory.mkdir()) {
            System.out.println("DynamicConfiguration root directory created : " + rootDirectoryPath);
        }else {
            System.out.println("DynamicConfiguration root directory already existed : " + rootDirectoryPath);
        }

        List<TreeItem> fileItems = repo.getTree(adapterConfiguration, "/", branch,true).stream()
                                    .filter(item -> {
                                        //System.out.println(item.toString());
                                        if(item.getType().equals(TreeItem.Type.TREE)){
                                            //String dir = "./"+item.getName();
                                            String dir = rootDirectoryPath.concat("/").concat(item.getPath());
                                            File f1 = new File(dir);
                                            if(f1.mkdir()) {
                                                System.out.println("DynamicConfiguration sub-directory created : " + dir);
                                            }else {
                                                System.out.println("DynamicConfiguration sub-directory already existed : " + dir);
                                            }
                                            return false;
                                        }
                                        return item.getType().equals(TreeItem.Type.BLOB);
                                    })
                                    .collect(Collectors.toList());

        RepositoryFile repoFile = null;
        for(TreeItem file : fileItems){
            repoFile = gitLabApi.getRepositoryFileApi().getFile(adapterConfiguration,file.getPath(),branch);
            //String path = new File(".").getCanonicalPath() + "/".concat(repoFile.getFilePath());
            String path = rootDirectoryPath.concat("/").concat(repoFile.getFilePath());
            try {
                File newFile = new File(path);
                newFile.createNewFile();
                Files.write(Paths.get(path), repoFile.getDecodedContentAsBytes());
                System.out.println("DynamicConfiguration file created/updated : " + path);
            }catch (Exception e){
                System.out.println("DynamicConfiguration file creation/updation failed : " + path);
                e.printStackTrace();
            }
        }
    }
}
