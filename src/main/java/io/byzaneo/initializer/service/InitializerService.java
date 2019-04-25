package io.byzaneo.initializer.service;

import io.byzaneo.initializer.Constants;
import io.byzaneo.initializer.bean.Project;
import io.byzaneo.initializer.data.ProjectRepository;
import io.byzaneo.initializer.event.*;
import io.byzaneo.initializer.facet.Facet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import static io.byzaneo.initializer.Constants.Mode.create;
import static io.byzaneo.initializer.Constants.Mode.*;
import static io.byzaneo.initializer.Constants.TIMEOUT;
import static io.byzaneo.one.SecurityContext.userEmail;
import static io.byzaneo.one.SecurityContext.userName;
import static java.time.Duration.between;
import static java.time.Instant.now;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;
import static org.springframework.core.Ordered.LOWEST_PRECEDENCE;
import static org.springframework.util.Assert.notNull;
import static reactor.core.publisher.Mono.*;

@Slf4j
@Service
public class InitializerService {

    static final String CONDITION_CREATE = "#event.project.mode == T(io.byzaneo.initializer.Constants$Mode).create";
    static final String CONDITION_UPDATE = "#event.project.mode == T(io.byzaneo.initializer.Constants$Mode).update";
    static final String CONDITION_DELETE = "#event.project.mode == T(io.byzaneo.initializer.Constants$Mode).delete";

    private final ApplicationEventPublisher publisher;
    private final ProjectRepository projects;
    private Map<String, TreeSet<String>> facetNamesByFamilies;

    public InitializerService(ApplicationEventPublisher publisher, ProjectRepository projects) {
        this.publisher = publisher;
        this.projects = projects;
    }

    public Mono<Project> create(@NotNull Project project) {
        return this.publish(project, create);
    }

    public Mono<Project> update(@NotNull Project project) {
        return this.publish(project, update);
    }

    public Mono<Project> delete(@NotNull Project project) {
        return this.publish(project, delete);
    }

    private Mono<Project> publish(@NotNull Project project, @NotNull Constants.Mode mode) {
        project.setMode(mode);
        return just(project)
                .flatMap(this::authenticate)
                .map(p -> this.resolve(project, mode))
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
                .map(ProjectPersistenceEvent::new)
                .map(this::publish)
                .map(ProjectPostEvent::new)
                .map(this::publish);
    }

    private Project publish(ProjectEvent event) {
        try {
            this.publisher.publishEvent(event);
        } catch (Exception e) {
            // publishes project error event
            // to be able to manage rollbacks
            log.error("Initializer failure on event: "+event, e);
            this.publisher.publishEvent(new ProjectErrorEvent(event, e));
            throw e;
        }
        return event.getProject();
    }

    private Project resolve(Project project, Constants.Mode mode) {
        switch (mode) {
            case create:
                return project;
            case update:
                notNull(project.getId(), "Project identifier (id) is required");
                return project;
            case delete:
                return (project.getId()!=null
                        ? this.projects.findById(project.getId())
                        : this.projects.findByNameAndOwner(project.getName(), project.getOwner()))
                    .doOnNext(p -> p.setMode(mode))
                    // lets work with the given project...
                    .defaultIfEmpty(project)
                    .block(TIMEOUT);
        }
        throw new IllegalArgumentException("Mode not supported: "+mode);
    }

    private Mono<Project> authenticate(@NotNull Project project) {
        log.debug("user: {} in mode: {}", project.getOwner(), project.getMode());

        return userEmail()
                .doOnNext(project::setOwner)
                .switchIfEmpty(defer(() -> error(new IllegalArgumentException("not.account.owner"))))
                .then(userEmail())
                .filter(owner -> owner.equals(project.getOwner()))
                .switchIfEmpty(defer(() -> error(new InsufficientAuthenticationException("Action restricted to the p owner"))))
                .then(userName())
                .doOnNext(project::setOwnerName) // null if empty
                .then(just(project));
    }

    /* -- FACETS -- */

    @EventListener
    public void onLoadFacets(@NotNull ContextRefreshedEvent event) {
        this.facetNamesByFamilies = new HashMap<>();
        event.getApplicationContext()
                .getBeansOfType(Facet.class)
                .forEach((name, facet) ->
                        this.facetNamesByFamilies
                                .computeIfAbsent(facet.getFamily().toString(), s -> new TreeSet<>())
                                .add(name));
        log.info("Facets: {}", this.facetNamesByFamilies);
    }

    public Map<String, TreeSet<String>> getFacetNamesByFamilies() {
        return facetNamesByFamilies;
    }

    /* -- DATA -- */

    @EventListener(condition = CONDITION_CREATE + " or " + CONDITION_UPDATE)
    public void onSaveProject(@NotNull ProjectPersistenceEvent event) {
        this.projects.save(event.getProject())
                .blockOptional(TIMEOUT)
                .ifPresent(p -> log.info("Project saved: {}", p));
    }

    @EventListener(condition = CONDITION_DELETE)
    public void onDeleteProject(@NotNull ProjectPersistenceEvent event) {
        if ( event.getProject().getId()!=null )
            this.projects.delete(event.getProject())
                    .blockOptional(TIMEOUT)
                    .ifPresent(p -> log.info("Project deleted: {}", p));
        else
            log.warn("Project not found for deletion: {}", event.getProject());
    }

    /* -- LOGS -- */

    @EventListener
    @Order(HIGHEST_PRECEDENCE)
    public void onStartProject(@NotNull ProjectPreEvent event) {
        log.info("{} {} project at {}",
                event.getMode(),
                event.getProject().getName(),
                event.getProject().getDate());
    }

    @EventListener
    @Order
    public void onEndProject(@NotNull ProjectPostEvent event) {
        log.info("{} {} project ends in {}ms",
                event.getMode(),
                event.getProject().getName(),
                between(event.getProject().getDate(), now()).toMillis());
    }

    @EventListener
    @Order(HIGHEST_PRECEDENCE + 1)
    public void onStartEvent(@NotNull ProjectEvent event) {
        log.debug("{} project {} event starts",
                event.getProject().getName(),
                event.getName());
    }

    @EventListener
    @Order(LOWEST_PRECEDENCE - 1)
    public void onEndEvent(@NotNull ProjectEvent event) {
        log.debug("{} project {} event ends in {}ms",
                event.getProject().getName(),
                event.getName(),
                between(event.getDate(), now()).toMillis());
    }

    @EventListener
    public void onError(@NotNull ProjectErrorEvent event) {
        log.error(event.toString());
    }
}

