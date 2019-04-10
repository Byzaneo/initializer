package io.byzaneo.initializer.service;

import io.byzaneo.initializer.Constants;
import io.byzaneo.initializer.bean.Project;
import io.byzaneo.initializer.event.ProjectIntegrationEvent;
import io.byzaneo.initializer.event.ProjectPreEvent;
import io.byzaneo.initializer.facet.CodeClimate;
import io.byzaneo.initializer.facet.GitHub;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

import static io.byzaneo.initializer.service.InitializerService.CONDITION_CREATE;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.util.StringUtils.hasText;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;

// TODO : remove repo not available in the code climate API
@Slf4j
@Service
public class CodeClimateService {

    private static final String CONDITION_CODE_CLIMATE = "#event.project.quality?.id == T(io.byzaneo.initializer.facet.CodeClimate).FACET_ID";

    private final String defaultToken;
    private final String defaultApi;

    public CodeClimateService(
            @Value("${initializer.codeclimate.token}") String defaultToken,
            @Value("${initializer.codeclimate.api}") String defaultApi) {
        this.defaultToken = defaultToken;
        this.defaultApi = defaultApi;
    }

    /* -- EVENTS -- */

    @EventListener(condition = CONDITION_CODE_CLIMATE)
    public void onInit(ProjectPreEvent event) {
        final CodeClimate codeClimate = (CodeClimate) event.getProject().getQuality();
        if (!hasText(codeClimate.getToken()))
            codeClimate.setToken(this.defaultToken);
        if (!hasText(codeClimate.getApi()))
            codeClimate.setApi(this.defaultApi);

        log.info("CodeClimate: {}", codeClimate.getApi());
    }

    @EventListener(condition = CONDITION_CREATE + " and " + CONDITION_CODE_CLIMATE)
    public void onInit(ProjectIntegrationEvent event) {
        // present repository facet means a repository
        // should have been created
        if (event.getProject().getRepository() != null)
            // activates code climate on the new repository
            this.activation(event.getProject());
    }

    /* -- PRIVATE -- */

    private void activation(Project project) {
        final CodeClimate codeClimate = (CodeClimate) project.getQuality();
        final GitHub github = (GitHub) project.getRepository();

        client(codeClimate)
                .post()
                .uri("/v1/github/repos")
                .body(fromObject(toResource(github)))
                .exchange()
                .doOnSuccess(response -> {
                    if (CREATED.equals(response.statusCode())) {
                        log.info("CodeClimate: {} triggered", github.getName());
                    } else
                        log.warn("CodeClimate: {} triggering failed ({})", github.getName(), response.rawStatusCode());
                })
                .block(Constants.TIMEOUT);
    }

    /**
     * https://jsonapi.org/format/#crud-creating
     */
    private Map<String, Map<String, Object>> toResource(GitHub github) {
        return Map.of("data", Map.of(
                "type", "repos",
                "attributes", Map.of("url", github.getHome() + github.getOrganization() + "/" + github.getName())));
    }

    private WebClient client(CodeClimate codeClimate) {
        return WebClient.builder()
                .baseUrl(codeClimate.getApi())
                .defaultHeader(AUTHORIZATION, "Token token=" + codeClimate.getToken())
                .defaultHeader(ACCEPT, "application/vnd.api+json") // use to POST
                .defaultHeader(CONTENT_TYPE, "application/vnd.api+json") // use to POST
                .build();
    }
}
