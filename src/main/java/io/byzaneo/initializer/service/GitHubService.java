package io.byzaneo.initializer.service;

import io.byzaneo.initializer.RepositoryException;
import io.byzaneo.initializer.bean.Project;
import io.byzaneo.initializer.event.ProjectPostEvent;
import io.byzaneo.initializer.event.ProjectRepositoryEvent;
import io.byzaneo.initializer.event.ProjectSourcesEvent;
import io.byzaneo.initializer.facet.GitHub;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;
import static org.springframework.core.Ordered.LOWEST_PRECEDENCE;

@Slf4j
@Service
public class GitHubService {

    private static final String CONDITION_CREATE =
            "#event.project.mode == T(io.byzaneo.initializer.Constants$Mode).create and " +
            "#event.project.repository?.name == T(io.byzaneo.initializer.facet.GitHub).FACET_NAME";

    private final String defaultToken;
    private final String defaultOrganization;

    public GitHubService(
            @Value("${initializer.github.token}") String defaultToken,
            @Value("${initializer.github.organization}") String defaultOrganization) {
        this.defaultToken = defaultToken;
        this.defaultOrganization = defaultOrganization;
    }


    /* -- EVENTS -- */

    @EventListener(condition = CONDITION_CREATE)
    @Order(HIGHEST_PRECEDENCE + 10)
    public void onCreateRepository(ProjectRepositoryEvent event) throws IOException, GitAPIException {
        final Project project = event.getProject();
        final GitHub github = (GitHub) project.getRepository();

        log.info("Creating GitHub repository: {}/{}", github.getOrganization(), project.getName());
        // resolves the token
        github.setToken(ofNullable(github.getToken())
                .orElse(this.defaultToken));
        // resolves the organization
        github.setOrganization(ofNullable(github.getOrganization())
                .orElse(this.defaultOrganization));
        // creates the GitHub repository
        github.setRepository(this.repositoryService(github).createRepository(
                github.getOrganization(),
                this.toRepository(project)));
        // clone repo in the project's directory
        github.setGit(Git.cloneRepository()
                .setURI(github.getRepository().getCloneUrl())
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(github.getToken(), "" ))
                .setDirectory(project.getDirectory().toFile())
                .call());
    }

    @EventListener(condition = CONDITION_CREATE)
    @Order(LOWEST_PRECEDENCE - 5)
    public void onCommitSources(ProjectSourcesEvent event) throws IOException {
        final Project project = event.getProject();
        final GitHub github = (GitHub) project.getRepository();
        final Git git = github.getGit();
        log.info("Committing sources to {}", github.getRepository().getCloneUrl());

        try (final Stream<Path> sources = Files.list(project.getDirectory())) {
            // Adds files to the index
            sources.map(Path::getFileName)
                .map(Path::toString)
                .filter(fn -> !".git".equals(fn))
                .forEach(fn -> {
                    try {
                        git.add().addFilepattern(fn).call();
                        log.debug("./{} added", fn);
                    } catch (GitAPIException e) {
                        throw new RepositoryException("Git add failed for file name: "+fn);
                    }
                });

            // commits
            final RevCommit commit = git
                    .commit()
                    .setAuthor(project.getOwnerName(), project.getOwner())
                    .setMessage("Initial commit")
                    .call();
            log.info("= {}", commit);

            // pushes
            git.push()
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(github.getToken(), "" ))
                    .call()
                    .forEach(pr -> log.info("= push: {}", pr.getMessages()));

        } catch (Exception e) {
            throw new RepositoryException("Failed to commit project "+project.getName()+
                    " sources from: "+project.getDirectory(), e);
        }
    }

    @EventListener(condition = CONDITION_CREATE)
    @Order(LOWEST_PRECEDENCE - 5)
    public void onPost(ProjectPostEvent event) throws IOException {
        final Project project = event.getProject();
        final GitHub github = (GitHub) project.getRepository();
        log.debug("Closing git repository connection");
        github.getGit().close();
    }

    /* -- PRIVATE -- */

    private RepositoryService repositoryService(GitHub github) {
        final GitHubClient client = new GitHubClient();
        client.setOAuth2Token(github.getToken());
        return new RepositoryService(client);
    }

    private Repository toRepository(Project project) {
        final Repository repository = new Repository();
        repository.setName(project.getName());
        repository.setDescription(project.getDescription());
        repository.setHomepage("https://byzaneo.io/account/projects/"+project.getName());
        repository.setPrivate(false);
        repository.setHasIssues(true);
        repository.setHasWiki(false);
        return repository;
    }

}
