# GitLabJ-module (https://hub.docker.com/r/aviorn36/gitsyncmodule)
This modules can be integrated with your application to provide runtime-synchronization of config-files

Functionality:
1. Initialization - this module connects and downloads/save files&folders from GitLab to application server with environment configurations specified.
2. Runtime - /reloadDynamicFlowConfigurationV2, can be used to dynamically change the downloaded files&folder of every commit.

How to sync any repo in gitLab with this module.

Locally : Pass in environment variables (as mentioned below) and run locally.

DockerImage : Build docker image and run with environment variables (as mentioned below).\

(or use this to pull image : docker pull aviorn36/gitsyncmodule)

Docker command :
docker run -d \ 
--name container_name \
-p host_port:container_port \
-e SERVER_PORT=application_port \
-e GIT_SERVER_URL=gitlab_server_url \
-e GIT_SERVER_TOKEN=gitlab_private_token \
-e GIT_SERVER_REPO=gitlab_repository_name \
-e GIT_SERVER_BRANCH=gitlab_branch \
-e CONFIG_ROOT_PATH=application_config_root_folder \
-e HOOK_SECRET_TOKEN=gitlab_header_token \
dockerImage:tag

Note :
1. container_port should be same as application_port
2. gitlab_server_url like "https://gitlab.com"
3. gitlab_private_token generated in gitLab token settings.
4. application_config_root_folder is the root folder at the application side where the files/folders of repository would be saved in.
5. Web-hook to application EP "/reloadDynamicFlowConfigurationV2" with merge/push event can be configured at git lab.
