package io.byzaneo.initializer.service;

import io.byzaneo.initializer.bean.Project;
import io.byzaneo.initializer.event.ProjectPreEvent;
import io.byzaneo.initializer.event.ProjectRepositoryEvent;
import io.byzaneo.initializer.facet.Travis;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static java.util.Optional.ofNullable;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;
import static org.springframework.util.StringUtils.hasText;

@Slf4j
@Service
public class TravisService {

    @Value("${initializer.travis.token}")
    private String defaultToken;

    @Value("${initializer.travis.api}")
    private String defaultApi;

    /* -- EVENTS -- */

    @EventListener(condition = "#event.project.integration?.name == T(io.byzaneo.initializer.facet.Travis).FACET_NAME")
    public void onInit(ProjectPreEvent event) {
        Travis travis = (Travis) event.getProject().getIntegration();
        if ( !hasText(travis.getToken()) )
            travis.setToken(this.defaultToken);
        if ( !hasText(travis.getApi()) )
            travis.setApi(this.defaultApi);
    }

    @EventListener(condition = "#event.project.mode == T(io.byzaneo.initializer.Constants$Mode).create and #event.project.integration.name == T(io.byzaneo.initializer.facet.Travis).FACET_NAME")
    @Order(HIGHEST_PRECEDENCE + 10)
    public void createReposioty(ProjectRepositoryEvent event) throws IOException {
        final Project project = event.getProject();
        final Travis travis = (Travis) project.getIntegration();

        // TODO puts api and token in properties

        log.info("Configuring Travis integration: {}", project.getName());

    }

    /* -- PRIVATE -- */

    private String api(Travis travis) {
        return ofNullable(travis.getApi())
                .orElse(this.defaultApi);
    }

    private String token(Travis travis) {
        return ofNullable(travis.getToken())
                .orElse(this.defaultToken);
    }


}
