package com.aviorn36.GitSync;

import com.aviorn36.GitSync.GitSynchronization.services.GitSyncServices;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.gitlab4j.api.GitLabApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.io.File;
import java.io.IOException;

@SpringBootApplication
public class GitSyncSpringApplication {

	@Autowired
	private GitSyncServices gitSyncServices;

	@Value("${application.flow.configRootPath}")
	private String dynamicConfigRootPath;

	public static void main(String[] args) {
		SpringApplication.run(GitSyncSpringApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationStartUp() throws GitLabApiException, IOException {
		String dynamicConfigurationRootPath = "./".concat(dynamicConfigRootPath);
		FileUtils.deleteDirectory(new File(dynamicConfigurationRootPath));
		System.out.println("Root config files/folders deleted : " + dynamicConfigurationRootPath);
		System.out.println("Retrieving all flow configuration...");
		gitSyncServices.loadAllConfiguration();
		System.out.println("Flow configuration created...");
	}

}
