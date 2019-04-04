package io.byzaneo.initializer.service;

import io.byzaneo.initializer.event.ProjectPreEvent;
import io.byzaneo.initializer.facet.Docker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import static org.springframework.util.StringUtils.hasText;

@Slf4j
@Service
public class DockerService {

    private static final String CONDITION_DOCKER =
            "#event.project.assembly?.name == T(io.byzaneo.initializer.facet.Docker).FACET_NAME";

    @Value("${initializer.docker.registry}")
    private String defaultRegistry;

    @Value("${initializer.docker.username}")
    private String defaultUsername;

    @Value("${initializer.docker.password}")
    private String defaultPassword;


    /* -- EVENTS -- */

    @EventListener(condition = CONDITION_DOCKER)
    public void onInit(ProjectPreEvent event) {
        Docker docker = (Docker) event.getProject().getAssembly();
        if ( !hasText(docker.getRegistry()) )
            docker.setRegistry(this.defaultRegistry);
        if ( !hasText(docker.getUsername()) )
            docker.setUsername(this.defaultUsername);
        if ( !hasText(docker.getPassword()) )
            docker.setPassword(this.defaultPassword);
    }

    /* -- PRIVATE -- */

}
