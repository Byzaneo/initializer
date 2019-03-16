package io.byzaneo.initializer.service;

import io.byzaneo.initializer.bean.Project;
import io.byzaneo.initializer.event.ProjectRepositoryEvent;
import io.byzaneo.initializer.facet.GitHub;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static java.util.Optional.ofNullable;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@Slf4j
@Service
public class GitHubService {

    @Value("${initializer.github.token}")
    private String defaultToken;

    @Value("${initializer.github.organization}")
    private String defaultOrganization;

    /* -- EVENTS -- */

    @EventListener(condition = "#event.project.mode == T(io.byzaneo.initializer.Constants$Mode).create and #event.project.repository.name == T(io.byzaneo.initializer.facet.GitHub).FACET_NAME")
    @Order(HIGHEST_PRECEDENCE + 10)
    public void createReposioty(ProjectRepositoryEvent event) throws IOException {
        final Project project = event.getProject();
        final GitHub github = (GitHub) project.getRepository();
        final String organization = organization(github);

        log.info("Creating GitHub repository: {}/{}", organization, project.getName());

        // TODO puts organization in properties

//        this.repositoryService(github).createRepository(
//                organization,
//                this.createRepository(project)
//        );
    }

    /* -- PRIVATE -- */

    private RepositoryService repositoryService(GitHub github) {
        final GitHubClient client = new GitHubClient();
        client.setOAuth2Token(ofNullable(github.getToken()).orElse(this.defaultToken));
        return new RepositoryService(client);
    }

    private String organization(GitHub github) {
        return ofNullable(github.getOrganization())
                .orElse(this.defaultOrganization);
    }

    private Repository createRepository(Project project) {
        final Repository repository = new Repository();
        repository.setName(project.getName());
        repository.setDescription(project.getDescription());
        repository.setHomepage("https://byzaneo.io/catalog/services/test");
        repository.setPrivate(false);
        repository.setHasIssues(true);
        repository.setHasWiki(false);
        return repository;
    }

}
