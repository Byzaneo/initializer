package io.byzaneo.initializer.service;

import io.byzaneo.initializer.bean.Project;
import io.byzaneo.initializer.event.ProjectIntegrationEvent;
import io.byzaneo.initializer.event.ProjectPostEvent;
import io.byzaneo.initializer.event.ProjectPreEvent;
import io.byzaneo.initializer.event.ProjectRepositoryEvent;
import io.byzaneo.initializer.facet.Travis;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

import static io.byzaneo.initializer.service.InitializerService.CONDITION_CREATE;
import static java.time.Duration.ofSeconds;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;
import static org.springframework.core.Ordered.LOWEST_PRECEDENCE;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.util.StringUtils.hasText;
import static org.springframework.web.reactive.function.BodyInserters.fromFormData;

@Slf4j
@Service
public class TravisService {

    private static final String CONDITION_TRAVIS = "#event.project.integration?.id == T(io.byzaneo.initializer.facet.Travis).FACET_ID";
    private static final Duration TIMEOUT = ofSeconds(15);
    private static final String PROJECT_VARS = "PROJECT";

    private final String defaultToken;
    private final String defaultApi;

    public TravisService(
            @Value("${initializer.travis.token}") String defaultToken,
            @Value("${initializer.travis.api}") String defaultApi) {
        this.defaultToken = defaultToken;
        this.defaultApi = defaultApi;
    }

    /* -- EVENTS -- */

    @EventListener(condition = CONDITION_TRAVIS)
    public void onInit(ProjectPreEvent event) {
        final Travis travis = (Travis) event.getProject().getIntegration();
        if ( !hasText(travis.getToken()) )
            travis.setToken(this.defaultToken);
        if ( !hasText(travis.getApi()) )
            travis.setApi(this.defaultApi);

        log.info("Travis: {}", travis.getApi());
    }

    @EventListener(condition = CONDITION_CREATE + " and " + CONDITION_TRAVIS)
    @Order(LOWEST_PRECEDENCE - 10)
    public void onRepositoryCreated(ProjectRepositoryEvent event) {
        // present repository facet means a repository
        // should have been created
        if ( event.getProject().getRepository()!=null )
            // disables travis on the new repository
            this.activation(event.getProject(), false);
    }

    @EventListener(condition = CONDITION_CREATE + " and " + CONDITION_TRAVIS)
    @Order(HIGHEST_PRECEDENCE + 10)
    public void onCreateIntegration(ProjectIntegrationEvent event) {
        final Project project = event.getProject();
        final Travis travis = (Travis) project.getIntegration();
        final String slug = project.getRepository().getSlug();

        log.info("Configuring Travis integration on: {}", slug);

        // adds project environment variables
        this.variables(travis, slug, PROJECT_VARS, "NAME", project.getName());
        this.variables(travis, slug, PROJECT_VARS, "DESCRIPTION", project.getDescription());
        this.variables(travis, slug, PROJECT_VARS, "OWNER_NAME", project.getOwnerName());
        this.variables(travis, slug, PROJECT_VARS, "OWNER_EMAIL", project.getOwner());
        log.info("Travis project {} env vars added on {}", project.getName(), slug);

        // adds facets environment variables
        project.facets()
            .filter(f -> !Travis.FACET_ID.equals(f.getId()))
            .forEach(facet -> facet.toProperties()
                .entrySet()
                .stream()
                .forEach(e -> this.variables(travis, slug, facet.getFamily().toString(),
                        e.getKey(), e.getValue())));
        log.info("Travis facets env vars added on {}", slug);
    }

    @EventListener(condition = CONDITION_CREATE + " and " + CONDITION_TRAVIS)
    public void onProjectCreated(ProjectPostEvent event) {
        // activates the project
        this.activation(event.getProject(), true);
        // triggers first build
        this.trigger(event.getProject());
    }

    /* -- PRIVATE -- */

    private void activation(Project project, boolean activate) {
        final Travis travis = (Travis) project.getIntegration();
        final String slug = project.getRepository().getSlug();
        final String prefix = activate ? "" : "de";

        client(travis)
            .post()
            .uri("/repo/{slug}/{de}activate", slug, prefix)
            .exchange()
            .doOnSuccess(response -> {
                if (OK.equals(response.statusCode()))
                    log.info("Travis: {} {}activated", slug, prefix);
                else
                    log.warn("Travis: {} {}activation failed ({})", slug, prefix, response.rawStatusCode());
            })
            .block(TIMEOUT);
    }

    private void variables(Travis travis, String slug, String keyPrefix, String key, Object value) {
        // skips null value
        if ( value==null )
            return;

        // env var key: [KEY_PREFIX]_[KEY]
        final String var = (keyPrefix + "_" + key).toUpperCase();
        client(travis)
                .post()
                .uri("/repo/{slug}/env_vars", slug)
                .body(fromFormData("env_var.name", var)
                        .with("env_var.value", value.toString())
                        // only property name which contains 'password' or 'token' will be secret
                        .with("env_var.public", Boolean.toString(!(
                                key.toLowerCase().contains("password") ||
                                key.toLowerCase().contains("token")))))
                .exchange()
                .doOnSuccess(response -> {
                    if (CREATED.equals(response.statusCode()))
                        log.debug("variable: {}", var);
                    else if (CONFLICT.equals(response.statusCode()))
                        log.warn("variable already exists: {} ({})", var, response.rawStatusCode());
                    else // should we throw an exception
                        log.error("Travis {} variable failed: {} ({})", slug, var, response.rawStatusCode());
                })
                .block(TIMEOUT);
    }

    private void trigger(Project project) {
        final Travis travis = (Travis) project.getIntegration();
        final String slug = project.getRepository().getSlug();

        client(travis)
                .post()
                .uri("/repo/{slug}/requests", slug)
                .body(fromFormData("request.message", "First build")
                        .with("request.branch", "master"))
                .exchange()
                .doOnSuccess(response -> {
                    if (ACCEPTED.equals(response.statusCode()))
                        log.info("Travis: {} triggered", slug);
                    else
                        log.warn("Travis: {} triggering failed ({})", slug, response.rawStatusCode());
                })
                .block(TIMEOUT);
    }

    private WebClient client(Travis travis) {
        return WebClient.builder()
            .baseUrl(travis.getApi())
            .defaultHeader(AUTHORIZATION, "token "+travis.getToken())
            .defaultHeader("Travis-API-Version", "3")
            .defaultHeader("User-Agent", "API Explorer")
            .defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .build();
    }
}
