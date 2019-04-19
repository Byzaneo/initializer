package io.byzaneo.initializer.service;

import io.byzaneo.initializer.InitializerException;
import io.byzaneo.initializer.bean.Project;
import io.byzaneo.initializer.event.*;
import io.byzaneo.initializer.facet.GitHub;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.PullRequestMarker;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.CollaboratorService;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import static io.byzaneo.initializer.Constants.Mode.create;
import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;
import static org.springframework.core.Ordered.LOWEST_PRECEDENCE;
import static org.springframework.util.StringUtils.hasText;

@Slf4j
@Service
public class GitHubService {

    private static final String CONDITION_GITHUB = "#event.project.repository?.id == T(io.byzaneo.initializer.facet.GitHub).FACET_ID";
    private static final String CONDITION_CREATE = InitializerService.CONDITION_CREATE + " and " + CONDITION_GITHUB;
    private static final String CONDITION_UPDATE = InitializerService.CONDITION_UPDATE + " and " + CONDITION_GITHUB;
    private static final String CONDITION_DELETE = InitializerService.CONDITION_DELETE + " and " + CONDITION_GITHUB;

    private final BuildProperties buildProperties;
    private final String defaultToken;
    private final String defaultOrganization;

    public GitHubService(
            @Value("${initializer.github.token}") String defaultToken,
            @Value("${initializer.github.organization}") String defaultOrganization,
            BuildProperties buildProperties) {
        this.defaultToken = defaultToken;
        this.defaultOrganization = defaultOrganization;
        this.buildProperties = buildProperties;
    }


    /* -- EVENTS -- */

    @EventListener(condition = CONDITION_GITHUB)
    public void onInit(ProjectPreEvent event) throws IOException {
        final GitHub github = (GitHub) event.getProject().getRepository();

        // resolves the repository name
        github.setName(ofNullable(github.getName())
                .orElse(event.getProject().getName()));
        // resolves the token
        github.setToken(ofNullable(github.getToken())
                .orElse(this.defaultToken));
        // resolves the organization
        github.setOrganization(ofNullable(github.getOrganization())
                .orElse(this.defaultOrganization));
        // resolves the username
        github.setUsername(ofNullable(github.getUsername())
                .orElse(user(github).getLogin()));

        // sanity checks
        log.info("GitHub: {}", github.getSlug());

        // gets the project's repository
        if (!create.equals(event.getProject().getMode())) {
            try {
                github.setRepository(this.repositoryService(github)
                        .getRepository(github.getOwner(), github.getName()));
            } catch (RequestException re) {
                if (re.getStatus() == 404)
                    log.warn("GitHub repository not found: {}", github.getSlug());
                else
                    throw re;
            }
        }
    }

    /* - CREATE - */

    @EventListener(condition = CONDITION_CREATE)
    @Order(HIGHEST_PRECEDENCE + 10)
    public void onCreateRepository(ProjectRepositoryEvent event) throws GitAPIException, IOException {
        final Project project = event.getProject();
        final GitHub github = (GitHub) project.getRepository();

        log.info("Creating GitHub repository: {}", github.getSlug());

        // creates the GitHub repository
        final Repository repo = this.toRepository(project, github);
        github.setRepository(hasText(github.getOrganization())
                ? this.repositoryService(github).createRepository(github.getOrganization(), repo)
                : this.repositoryService(github).createRepository(repo));

        // invites user to collaborate
        this.collaboratorService(github)
                .addCollaborator(github.getRepository(), github.getUsername());

        // clone repo in the project's directory
        github.setGit(cloneRepository(project, github));
    }

    @EventListener(condition = CONDITION_CREATE)
    @Order(LOWEST_PRECEDENCE - 5)
    public void onCommitSources(ProjectSourcesEvent event) {
        final Project project = event.getProject();
        final GitHub github = (GitHub) project.getRepository();
        final Git git = github.getGit();
        log.info("Committing sources to {}", github.getRepository().getCloneUrl());

        try (final Stream<Path> sources = Files.list(project.getDirectory())) {
            // Adds files to the index
            addFile(git, sources);

            // commits
            final RevCommit commit = commit(git, "Initial commit");
            log.info("= {}", commit);

            // pushes
            push(github, git);

        } catch (Exception e) {
            throw new InitializerException("Failed to commit project " + project.getName() +
                    " sources from: " + project.getDirectory(), e);
        }
    }

    @EventListener(condition = CONDITION_CREATE)
    @Order(LOWEST_PRECEDENCE - 5)
    public void onPost(ProjectPostEvent event) {
        final Project project = event.getProject();
        final GitHub github = (GitHub) project.getRepository();
        log.debug("Closing git repository connection");
        if (github.getGit() != null)
            github.getGit().close();
    }

    /* - UPDATE - */

    @EventListener(condition = CONDITION_UPDATE)
    @Order(HIGHEST_PRECEDENCE + 10)
    public void onCloneRepository(ProjectRepositoryEvent event) throws GitAPIException, IOException {
        final Project project = event.getProject();
        final GitHub github = (GitHub) project.getRepository();

        log.info("Updating GitHub repository: {}", github.getSlug());

        // gets the Github repository
        final Repository repo = this.toRepository(project, github);
        github.setRepository(hasText(github.getOrganization())
                ? this.repositoryService(github).getRepository(github.getOrganization(), repo.getName())
                : this.repositoryService(github).getRepository(repo));

        // clone repo in the project's directory
        github.setGit(cloneRepository(project, github));
    }

    @EventListener(condition = CONDITION_UPDATE)
    @Order(LOWEST_PRECEDENCE - 5)
    public void onCreatePullRequest(ProjectSourcesEvent event) {
        final Project project = event.getProject();
        final GitHub github = (GitHub) project.getRepository();
        final Git git = github.getGit();
        final String branch = "update-"
                + this.buildProperties.getVersion() // TODO: test if it's beautiful
                + "-"
                + now().format(ofPattern("yyyy-MM-dd-HH-mm"));
        log.info("Updating sources to {}", github.getRepository().getCloneUrl());

        try (final Stream<Path> sources = Files.list(project.getDirectory())) {
            // Adds files to the index
            addFile(git, sources);

            // creates branch
            git.branchCreate()
                    .setName(branch)
                    .call();

            // checkout branch
            git.checkout()
                    .setName(branch)
                    .call();

            // commits
            final RevCommit commit = commit(git, "Update commit");
            log.info("= {}", commit);

            // pushes
            push(github, git);

            // creates pull request
            this.pullRequestService(github)
                    .createPullRequest(
                            github.getRepository(),
                            new PullRequest()
                                    .setTitle("Update from Initializer")
                                    .setHead(new PullRequestMarker()
                                            .setLabel(
                                                    (hasText(github.getOrganization())
                                                            ? github.getOrganization()
                                                            : github.getUsername())
                                                            + ":"
                                                            + branch))
                                    .setBase(new PullRequestMarker()
                                            .setLabel("master")));

        } catch (Exception e) {
            throw new InitializerException("Failed to create pull request on " + project.getName(), e);
        }
    }

    /* - DELETE - */

    @EventListener(condition = CONDITION_DELETE)
    @Order(LOWEST_PRECEDENCE - 10)
    public void onDeleteRepository(ProjectRepositoryEvent event) throws IOException {
        this.delete((GitHub) event.getProject().getRepository());
    }

    /* - ERROR - */

    @EventListener(condition = CONDITION_CREATE)
    public void onCreationError(ProjectErrorEvent error) {
        try {
            this.delete((GitHub) error.getProject().getRepository());
        } catch (IOException e) {
            log.error("Rollback error while deleting GitHub repository", e);
        }
    }

    @EventListener(condition = CONDITION_UPDATE)
    public void onUpdateError(ProjectErrorEvent error) {
        final Git git = error.getProject().getRepository().getGit();
        try {
            // removes update branch
            git.branchDelete().setBranchNames( // TODO: need to be test
                    git.getRepository().getBranch());
        } catch (IOException e) {
            log.error("Rollback error while deleting GitHub pull request", e);
        }
    }

    /* -- PRIVATE -- */

    private Repository toRepository(Project project, GitHub github) {
        final Repository repository = new Repository();
        repository.setName(github.getName());
        repository.setDescription(project.getDescription());
        repository.setHomepage("https://byzaneo.io/account/projects/" + project.getName());
        repository.setPrivate(false);
        repository.setHasIssues(true);
        repository.setHasWiki(false);
        return repository;
    }

    // GITHUB

    private Optional<GitHubClient> client(GitHub github) {
        final GitHubClient client = new GitHubClient();
        client.setOAuth2Token(github.getToken());
        return of(client);
    }

    private CollaboratorService collaboratorService(GitHub github) {
        return client(github)
                .map(CollaboratorService::new)
                .orElseThrow();
    }

    private PullRequestService pullRequestService(GitHub gitHub) {
        return client(gitHub)
                .map(PullRequestService::new)
                .orElseThrow();
    }

    private RepositoryService repositoryService(GitHub github) {
        return client(github)
                .map(RepositoryService::new)
                .orElseThrow();
    }

    private UserService userService(GitHub gitHub) {
        return client(gitHub)
                .map(UserService::new)
                .orElseThrow();
    }

    private User user(GitHub github) {
        try {
            return userService(github).getUser();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void delete(GitHub github) throws IOException {
        if (github.getRepository() != null) {
            log.info("Deleting GitHub repository: {}", github.getSlug());
            this.repositoryService(github).deleteRepository(github.getRepository());
        }
    }

    // GIT

    private void addFile(Git git, Stream<Path> sources) {
        sources.map(Path::getFileName)
                .map(Path::toString)
                .filter(fn -> !".git".equals(fn))
                .forEach(fn -> {
                    try {
                        git.add().addFilepattern(fn).call();
                        log.debug("./{} added", fn);
                    } catch (GitAPIException e) {
                        throw new InitializerException("Git addFile failed for file name: " + fn);
                    }
                });
    }

    private Git cloneRepository(Project project, GitHub github) throws GitAPIException {
        return Git.cloneRepository()
                .setURI(github.getRepository().getCloneUrl())
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(github.getToken(), ""))
                .setDirectory(project.getDirectory().toFile())
                .call();
    }

    private RevCommit commit(Git git, String message) throws GitAPIException {
        return git
                .commit()
                .setMessage(message)
                .call();
    }

    private void push(GitHub github, Git git) throws GitAPIException {
        git.push()
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(github.getToken(), ""))
                .call()
                .forEach(pr -> log.info("= push: {}", pr.getMessages()));
    }

}
