package io.byzaneo.initializer.service;

import io.byzaneo.initializer.bean.Project;
import io.byzaneo.initializer.event.ProjectEvent;
import io.byzaneo.initializer.event.ProjectInitializationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static java.util.Optional.ofNullable;

@Service
public class InitializerService {
    private final ApplicationEventPublisher publisher;


    public InitializerService(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public Optional<Project> create(Project project) {
        return ofNullable(project)
                .map(ProjectInitializationEvent::new)
                .map(this::publish);

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
                ;
*/
    }

    private Project publish(ProjectEvent event) {
        this.publisher.publishEvent(event);
        return event.getProject();
    }
}
