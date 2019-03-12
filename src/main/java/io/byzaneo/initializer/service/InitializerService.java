package io.byzaneo.initializer.service;

import io.byzaneo.initializer.Constants;
import io.byzaneo.initializer.bean.Project;
import io.byzaneo.initializer.event.*;
import io.byzaneo.initializer.facet.Facet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;

import static io.byzaneo.initializer.Constants.Mode.*;
import static java.time.Duration.between;
import static java.time.Instant.now;
import static java.util.Optional.ofNullable;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;
import static org.springframework.core.Ordered.LOWEST_PRECEDENCE;

@Slf4j
@Service
public class InitializerService {
    private final ApplicationEventPublisher publisher;
    private Map<String, TreeSet<String>> facetNamesByFamilies;

    public InitializerService(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public Optional<Project> create(@NotNull Project project) {
        return this.publish(project, create);
    }

    public Optional<Project> update(@NotNull Project project) {
        return this.publish(project, update);
    }

    public Optional<Project> delete(@NotNull Project project) {
        return this.publish(project, delete);
    }

    private Optional<Project> publish(@NotNull Project project, @NotNull Constants.Mode mode) {
        return ofNullable(project)
                .map(p -> p.toBuilder().mode(mode).build())
                .map(ProjectPreEvent::new)
                .map(this::publish)
                .map(ProjectRepositoryEvent::new)
                .map(this::publish)
                .map(ProjectSourcesEvent::new)
                .map(this::publish)
                .map(ProjectIntegrationEvent::new)
                .map(this::publish)
                .map(ProjectDeploymentEvent::new)
                .map(this::publish)
                .map(ProjectPersistencyEvent::new)
                .map(this::publish)
                .map(ProjectPostEvent::new)
                .map(this::publish);
    }

/*
                // - Repository -
                .doOnNext(this.publisher.publishEvent(new RepositoryCreation(project)))
                    // Repository-Github::create -> project.repo = https://...
                .doOnNext(this.publisher.publishEvent(new RepositoryCreated(project)))
                    // Integration-Travis::deactivate,
                    // Quality-CodeClimate::deactivate,
                    // Coverage-CodeCov::deactivate

                // - Sources -
                .doOnNext(this.publisher.publishEvent(new SourceCreation(project)))
                    // Language::source -> project.dir = /.../
                .doOnNext(this.publisher.publishEvent(new SourceCreated(project)))
                    // Management::source, Integration-Travis::source, Documentation-Readme::source, License::source
                .doOnNext(this.publisher.publishEvent(new SourcePush(project)))
                    // Repository-Github::push

                // - Deployment -
                .doOnNext(this.publisher.publishEvent(new DeploymentCreation(project)))
                    // Deployment-Spinnaker::create
                .doOnNext(this.publisher.publishEvent(new DeploymentCreated(project)))

                .doOnNext(this.publisher.publishEvent(new ProjectCreated(project)))
                    // Integration::activate, Quality:activate, Coverage::activate
*/

    private Project publish(ProjectEvent event) {
        this.publisher.publishEvent(event);
        return event.getProject();
    }

    /* -- FACETS -- */

    @EventListener
    public void loadFacets(@NotNull ContextRefreshedEvent event) {
        this.facetNamesByFamilies = new HashMap<>();
        event.getApplicationContext()
                .getBeansOfType(Facet.class)
                .forEach((name, facet) ->
                    this.facetNamesByFamilies
                            .computeIfAbsent(facet.getFamily().toString(), s -> new TreeSet<>())
                            .add(name));
        log.info("Facets: {}", this.facetNamesByFamilies);
    }

    /* -- LOGS -- */

    @EventListener
    @Order(HIGHEST_PRECEDENCE)
    public void startProject(@NotNull ProjectPreEvent event) {
        log.info("{} {} project at {}",
                event.getMode(),
                event.getProject().getName(),
                event.getProject().getDate());
    }

    @EventListener
    @Order
    public void endProject(@NotNull ProjectPostEvent event) {
        log.info("{} {} project ends in {}ms",
                event.getMode(),
                event.getProject().getName(),
                between(event.getProject().getDate(), now()).toMillis());
    }

    @EventListener
    @Order(HIGHEST_PRECEDENCE + 1)
    public void startEvent(@NotNull ProjectEvent event) {
        log.debug("{} project {} event starts",
                event.getProject().getName(),
                event.getName());
    }

    @EventListener
    @Order(LOWEST_PRECEDENCE - 1)
    public void endEvent(@NotNull ProjectEvent event) {
        log.debug("{} project {} event ends in {}ms",
                event.getProject().getName(),
                event.getName(),
                between(event.getDate(), now()).toMillis());
    }
}
