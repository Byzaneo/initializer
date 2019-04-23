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
            "#event.project.registry?.id == T(io.byzaneo.initializer.facet.Docker).FACET_ID";

    private final String defaultHostname;
    private final String defaultUsername;
    private final String defaultPassword;

    public DockerService(
            @Value("${initializer.docker.hostname}") String defaultHostname,
            @Value("${initializer.docker.username}") String defaultUsername,
            @Value("${initializer.docker.password}") String defaultPassword) {
        this.defaultHostname = defaultHostname;
        this.defaultUsername = defaultUsername;
        this.defaultPassword = defaultPassword;
    }


    /* -- EVENTS -- */

    @EventListener(condition = CONDITION_DOCKER)
    public void onInit(ProjectPreEvent event) {
        Docker docker = (Docker) event.getProject().getRegistry();
        if ( !hasText(docker.getHostname()) )
            docker.setHostname(this.defaultHostname);
        if ( !hasText(docker.getUsername()) )
            docker.setUsername(this.defaultUsername);
        if ( !hasText(docker.getPassword()) )
            docker.setPassword(this.defaultPassword);

        log.info("Docker: {}", docker.getHostname());
    }

    /* -- PRIVATE -- */

}
