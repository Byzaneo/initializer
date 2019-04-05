package io.byzaneo.initializer.service;

import io.byzaneo.initializer.bean.Project;
import io.byzaneo.initializer.event.ProjectIntegrationEvent;
import io.byzaneo.initializer.event.ProjectPreEvent;
import io.byzaneo.initializer.facet.Travis;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static io.byzaneo.initializer.service.InitializerService.CONDITION_CREATE;
import static java.util.Optional.ofNullable;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;
import static org.springframework.util.StringUtils.hasText;

@Slf4j
@Service
public class TravisService {

    public static final String CONDITION_TRAVIS = "#event.project.integration?.id == T(io.byzaneo.initializer.facet.Travis).FACET_ID";
    @Value("${initializer.travis.token}")
    private String defaultToken;

    @Value("${initializer.travis.api}")
    private String defaultApi;

    /* -- EVENTS -- */

    @EventListener(condition = CONDITION_TRAVIS)
    public void onInit(ProjectPreEvent event) {
        Travis travis = (Travis) event.getProject().getIntegration();
        if ( !hasText(travis.getToken()) )
            travis.setToken(this.defaultToken);
        if ( !hasText(travis.getApi()) )
            travis.setApi(this.defaultApi);
    }

    @EventListener(condition = CONDITION_CREATE + " and "+ CONDITION_TRAVIS)
    @Order(HIGHEST_PRECEDENCE + 10)
    public void onCreateIntegration(ProjectIntegrationEvent event) throws IOException {
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
