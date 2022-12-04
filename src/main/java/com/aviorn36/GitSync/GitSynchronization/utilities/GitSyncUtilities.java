package com.aviorn36.GitSync.GitSynchronization.utilities;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.RepositoryApi;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.RepositoryFile;
import org.gitlab4j.api.models.TreeItem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class GitSyncUtilities {

    public static Project getGitProject(GitLabApi gitLabApi, String repositoryName) throws GitLabApiException {
        List<Project> projects = gitLabApi.getProjectApi().getProjects(repositoryName);
        return projects.stream().filter( project -> project.getName().equalsIgnoreCase(repositoryName)).findFirst().get();
    }

    public static void createDirectory(String path) {
        File directory = new File(path);
        if(directory.mkdir()) {
            System.out.println("DynamicConfiguration directory created : " + path);
        }else {
            System.out.println("DynamicConfiguration directory already existed : " + path);
        }
    }

    private static void createFile(byte[] bytesInput, String path) throws IOException {
        File file = new File(path);
        file.createNewFile();
        Files.write(Paths.get(path), bytesInput);
    }

    private static void removeFile(String path) throws IOException {
        File file = new File(path);
        file.delete();
    }

    public static List<TreeItem> getAllFilesAndCreateDirectory(Project project, RepositoryApi repositoryClient, String branch, String rootDirectoryPath) throws GitLabApiException {
        return repositoryClient.getTree(project, "/", branch,true).stream()
                .filter(item -> {
                    if(item.getType().equals(TreeItem.Type.TREE)){
                        //System.out.println(rootDirectoryPath.concat("/").concat(item.getPath()));
                        createDirectory(rootDirectoryPath.concat("/").concat(item.getPath()));
                        return false;
                    }
                    return item.getType().equals(TreeItem.Type.BLOB);
                }).collect(Collectors.toList());
    }

    public static void createFilesFromTreeItems(GitLabApi gitLabApi, Project project, String branch, List<TreeItem> treeItems, String rootDirectoryPath) throws GitLabApiException {
        for(TreeItem file : treeItems){
            getAndCreateRemoteFile(gitLabApi, project, branch, rootDirectoryPath, file.getPath());
        }
    }

    private static void getAndCreateRemoteFile(GitLabApi gitLabApi, Project project, String branch, String rootDirectoryPath, String file) throws GitLabApiException {
        RepositoryFile repoFile = null;
        repoFile = gitLabApi.getRepositoryFileApi().getFile(project, file, branch);
        String path = rootDirectoryPath.concat("/").concat(repoFile.getFilePath());
        try {
            createFile(repoFile.getDecodedContentAsBytes(), path);
            System.out.println("DynamicConfiguration file created/updated : " + path);
        }catch (Exception e){
            System.out.println("DynamicConfiguration file creation/updation failed : " + path);
            e.printStackTrace();
        }
    }

    public static void processRemovedItem(String item) throws IOException {
        System.out.println("removing :" + item);
        removeFile(item);
    }

    public static void processAddedItem(GitLabApi gitLabApi, Project project, String branch,String dynamicConfigurationRootPath, String itemPath, String item) throws GitLabApiException {
        System.out.println("adding :" + item);
        createDirectory(dynamicConfigurationRootPath.concat("/").concat(itemPath));
        getAndCreateRemoteFile(gitLabApi, project, branch,dynamicConfigurationRootPath, item);
    }

    public static void processModifiedItem(GitLabApi gitLabApi, Project project, String branch,String dynamicConfigurationRootPath, String item) throws GitLabApiException {
        System.out.println("modifying :" + item);
        getAndCreateRemoteFile(gitLabApi, project, branch,dynamicConfigurationRootPath, item);
    }

}
