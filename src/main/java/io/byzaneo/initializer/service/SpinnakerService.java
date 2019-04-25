package io.byzaneo.initializer.service;

import io.byzaneo.initializer.InitializerException;
import io.byzaneo.initializer.bean.Project;
import io.byzaneo.initializer.event.ProjectDeploymentEvent;
import io.byzaneo.initializer.event.ProjectPreEvent;
import io.byzaneo.initializer.facet.Spinnaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import static io.byzaneo.initializer.Constants.TIMEOUT;
import static io.byzaneo.initializer.service.DockerService.CONDITION_DOCKER;
import static io.byzaneo.initializer.service.InitializerService.CONDITION_CREATE;
import static java.lang.String.format;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.*;
import static org.springframework.util.StringUtils.hasText;

@Slf4j
@Service
public class SpinnakerService {

    public static final String DEFAULT_PIPELINE = "prod";
    private static final String CONDITION_SPINNAKER = "#event.project.deployment?.id == T(io.byzaneo.initializer.facet.Spinnaker).FACET_ID";

    private static final String APPLICATION_TEMPLATE = Spinnaker.FACET_ID + "/application-create.yml";
    private static final String PIPELINE_TEMPLATE = Spinnaker.FACET_ID + "/pipeline-%s.yml";

    private final String api;
    private final String account;
    private final String pipeline;

    private final SourcesService sourcesService;

    public SpinnakerService(
            @Value("${initializer.spinnaker.api}") String api,
            @Value("${initializer.spinnaker.account}") String account,
            @Value("${initializer.spinnaker.pipeline}") String pipeline,
            SourcesService sourcesService) {
        this.api = api;
        this.account = account;
        this.pipeline = pipeline;
        this.sourcesService = sourcesService;
    }

    /* -- EVENTS -- */

    @EventListener(condition = CONDITION_SPINNAKER)
    public void onInit(ProjectPreEvent event) {
        final Spinnaker spinnaker = (Spinnaker) event.getProject().getDeployment();
        if ( !hasText(spinnaker.getApi()) )
            spinnaker.setApi(this.api);
        if ( !hasText(spinnaker.getAccount()) )
            spinnaker.setAccount(this.account);
        if ( !hasText(spinnaker.getPipeline()) )
            spinnaker.setPipeline(this.pipeline);

        log.info("Spinnaker: {} (account: {}, pipeline: {})",
                spinnaker.getApi(),
                spinnaker.getAccount(),
                spinnaker.getPipeline());
    }

    @EventListener(condition = CONDITION_CREATE
            + " and " + CONDITION_SPINNAKER
            + " and " + CONDITION_DOCKER) // only docker is supported as images provider for now
    @Order(HIGHEST_PRECEDENCE + 10)
    public void onCreateDeployment(ProjectDeploymentEvent event) {
        final Project project = event.getProject();
        this.createApplication(project);
        this.createPipeline(project);
    }

    /* -- PRIVATE -- */

    private void createApplication(Project project) {
        final Spinnaker spinnaker = (Spinnaker) project.getDeployment();

        this.client(spinnaker)
            .post()
            .uri("/applications/{name}/tasks", project.getName())
            .syncBody(this.sourcesService.transform(project, APPLICATION_TEMPLATE))
            .exchange()
            .doOnSuccess(response -> {
                if (CREATED.equals(response.statusCode()))
                    log.info("Spinnaker: {} application created", project.getName());
                else
                    throw new InitializerException("Spinnaker: failed to create application %s (%s)",
                            project.getName(), response.statusCode());
            })
            .block(TIMEOUT);
    }

    private void createPipeline(Project project) {
        final Spinnaker spinnaker = (Spinnaker) project.getDeployment();

        this.client(spinnaker)
            .post()
            .uri("/applications/pipelines")
            .syncBody(this.sourcesService.transform(project, format(PIPELINE_TEMPLATE, spinnaker.getPipeline())))
            .exchange()
            .doOnSuccess(response -> {
                if (OK.equals(response.statusCode()))
                    log.info("Spinnaker: {} pipeline created", spinnaker.getPipeline());
                else
                    throw new InitializerException("Spinnaker: failed to create pipeline %s (%s)",
                            spinnaker.getPipeline(), response.statusCode());
            })
            .block(TIMEOUT);
    }

    private WebClient client(Spinnaker spinnaker) {
        return WebClient.builder()
            .baseUrl(spinnaker.getApi())
            .defaultHeader(ACCEPT, APPLICATION_JSON_VALUE, TEXT_PLAIN_VALUE, ALL_VALUE)
            .defaultHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
            .build();
    }
}
