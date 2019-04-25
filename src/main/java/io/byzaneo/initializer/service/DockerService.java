package io.byzaneo.initializer.service;

import io.byzaneo.initializer.event.ProjectPreEvent;
import io.byzaneo.initializer.facet.Docker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import static org.apache.commons.lang.StringUtils.trimToEmpty;
import static org.springframework.util.StringUtils.hasText;

@Slf4j
@Service
public class DockerService {

    static final String CONDITION_DOCKER = "#event.project.registry?.id == T(io.byzaneo.initializer.facet.Docker).FACET_ID";

    private final String hostname;
    private final String library;
    private final String username;
    private final String password;
    private final String secret;

    public DockerService(
            @Value("${initializer.docker.hostname}") String hostname,
            @Value("${initializer.docker.library}") String library,
            @Value("${initializer.docker.username}") String username,
            @Value("${initializer.docker.password}") String password,
            @Value("${initializer.docker.secret}") String secret) {
        this.hostname = hostname;
        this.library = library;
        this.username = username;
        this.password = password;
        this.secret = secret;
    }


    /* -- EVENTS -- */

    @EventListener(condition = CONDITION_DOCKER)
    public void onInit(ProjectPreEvent event) {
        Docker docker = (Docker) event.getProject().getRegistry();
        if ( !hasText(docker.getHostname()) )
            docker.setHostname(this.hostname);
        if ( docker.getLibrary()==null ) // empty is allowed
            docker.setLibrary(trimToEmpty(this.library));
        if ( !hasText(docker.getUsername()) )
            docker.setUsername(this.username);
        if ( !hasText(docker.getPassword()) )
            docker.setPassword(this.password);
        if ( !hasText(docker.getSecret()) )
            docker.setSecret(this.secret);

        log.info("Docker: {}/{} ({})",
                docker.getImagePrefix(), event.getProject().getName(), docker.getSecret());
    }
}
