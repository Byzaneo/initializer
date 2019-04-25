package io.byzaneo.initializer.service;

import io.byzaneo.initializer.Constants;
import io.byzaneo.initializer.InitializerException;
import io.byzaneo.initializer.bean.Project;
import io.byzaneo.initializer.event.*;
import io.byzaneo.initializer.facet.GitHub;
import io.byzaneo.initializer.facet.Repository;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.PullRequestMarker;
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
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import static io.byzaneo.initializer.Constants.Mode.create;
import static io.byzaneo.initializer.Constants.Mode.delete;
import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;
import static org.springframework.core.Ordered.LOWEST_PRECEDENCE;
import static org.springframework.util.StringUtils.hasText;
import static org.springframework.util.StringUtils.startsWithIgnoreCase;

@Slf4j
@Service
public class GitHubService {

    private static final String CONDITION_GITHUB = "#event.project.repository?.id == T(io.byzaneo.initializer.facet.GitHub).FACET_ID";
    private static final String AND = " and ";
    private static final String CONDITION_CREATE = InitializerService.CONDITION_CREATE + AND + CONDITION_GITHUB;
    private static final String CONDITION_UPDATE = InitializerService.CONDITION_UPDATE + AND + CONDITION_GITHUB;
    private static final String CONDITION_DELETE = InitializerService.CONDITION_DELETE + AND + CONDITION_GITHUB;
    private static final String UPDATE_BRANCH_PREFIX = "update-";

    private final String defaultToken;
    private final String defaultOrganization;

    public GitHubService(
            @Value("${initializer.github.token}") String defaultToken,
            @Value("${initializer.github.organization}") String defaultOrganization) {
        this.defaultToken = defaultToken;
        this.defaultOrganization = defaultOrganization;
    }


    /* -- EVENTS -- */

    @EventListener(condition = CONDITION_GITHUB)
    public void onInit(ProjectPreEvent event) throws IOException, GitAPIException {
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

        // inits repository
        this.init(event.getProject(), github, event.getMode());
    }

    /* - CREATE - */

    @EventListener(condition = CONDITION_CREATE)
    @Order(HIGHEST_PRECEDENCE + 10)
    public void onCreateRepository(ProjectRepositoryEvent event) throws IOException {
        this.collaborator((GitHub) event.getProject().getRepository());
    }

    @EventListener(condition = CONDITION_CREATE)
    @Order(LOWEST_PRECEDENCE - 5)
    public void onCommitSources(ProjectSourcesEvent event) {
        this.commitSources(event.getProject(), "Initial commit");
    }

    /* - UPDATE - */

    @EventListener(condition = CONDITION_UPDATE)
    @Order(LOWEST_PRECEDENCE - 5)
    public void onCreatePullRequest(ProjectSourcesEvent event) throws GitAPIException, IOException {
        final Project project = event.getProject();
        final Git git = project.getRepository().getGit();
        final String branch = UPDATE_BRANCH_PREFIX + now().format(ofPattern("yyyyMMddHHmm"));

        // creates branch
        git.branchCreate().setName(branch).call();

        // switches to new branch
        git.checkout().setName(branch).call();

        // commits updated sources
        this.commitSources(project, "Update commit");

        // creates pull request
        this.pullRequest((GitHub) project.getRepository(), branch);
    }

    /* - DELETE - */

    @EventListener(condition = CONDITION_DELETE)
    @Order(LOWEST_PRECEDENCE - 10)
    public void onDeleteRepository(ProjectRepositoryEvent event) throws IOException {
        this.delete((GitHub) event.getProject().getRepository());
    }

    /* - COMMONS - */

    @EventListener(condition = CONDITION_GITHUB)
    public void onPost(ProjectPostEvent event) {
        log.debug("Closing git repository connection");
        ofNullable(event.getProject().getRepository().getGit())
                .ifPresent(Git::close);
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
            final String branch = git.getRepository().getBranch();
            if (startsWithIgnoreCase(branch, UPDATE_BRANCH_PREFIX)) {
                log.warn("Rollback git update branch {}", branch);
                git.branchDelete()
                        .setBranchNames(branch)
                        .call();
                this.push(error.getProject().getRepository(), git);
            }
        } catch (IOException | GitAPIException e) {
            log.error("Rollback error while deleting GitHub pull request", e);
        }
    }

    /* -- PRIVATE -- */

    // - GITHUB -

    private void init(Project project, GitHub github, Constants.Mode mode) throws IOException, GitAPIException {
        if ( github.getRepository()!=null )
            return;

        // creates the repository
        final RepositoryService repositoryService = this.repositoryService(github);
        if ( create.equals(mode) ) {
            log.info("Creating GitHub repository: {}", github.getSlug());

            // creates GitHub repository from
            // the facet's configuration
            final org.eclipse.egit.github.core.Repository repo = new org.eclipse.egit.github.core.Repository();
            repo.setName(github.getName());
            repo.setDescription(project.getDescription());
            repo.setHomepage("https://byzaneo.io/account/projects/" + project.getName());
            repo.setPrivate(false);
            repo.setHasIssues(true);
            repo.setHasWiki(false);

            // initializes the GitHub repository service
            github.setRepository(hasText(github.getOrganization())
                    ? repositoryService.createRepository(github.getOrganization(), repo)
                    : repositoryService.createRepository(repo));
        }
        // connects to the repository
        else {
            log.info("Getting GitHub repository: {}", github.getSlug());

            try {
                github.setRepository(repositoryService
                        .getRepository(github.getOwner(), github.getName()));
            } catch (RequestException re) {
                // skips error if the repository is not found
                // and we are in delete mode
                if (delete.equals(mode) && re.getStatus() == 404)
                    log.warn("GitHub repository not found: {}", github.getSlug());
                else
                    throw re;
            }
        }

        // clones the repository locally
        github.setGit(this.cloneRepository(project, github));
    }

    private Optional<GitHubClient> client(GitHub github) {
        final GitHubClient client = new GitHubClient();
        client.setOAuth2Token(github.getToken());
        return of(client);
    }

    private RepositoryService repositoryService(GitHub github) {
        return client(github)
                .map(RepositoryService::new)
                .orElseThrow();
    }

    private void collaborator(GitHub github) throws IOException {
        log.info("GitHub collaborator: {}", github.getUsername());
        final CollaboratorService cs = client(github)
                .map(CollaboratorService::new)
                .orElseThrow();
        cs.addCollaborator(github.getRepository(), github.getUsername());
    }

    private void pullRequest(GitHub github, String branch) throws IOException {
        log.info("GitHub Pull Request on branch: {}", branch);
        final PullRequestService prs = client(github)
                .map(PullRequestService::new)
                .orElseThrow();
        prs.createPullRequest(
            github.getRepository(),
            new PullRequest()
                    .setTitle("Initializer Update")
                    .setHead(new PullRequestMarker().setLabel(github.getOwner()+ ":" + branch))
                    .setBase(new PullRequestMarker().setLabel("master")));
    }

    private User user(GitHub github) throws IOException {
        final UserService us = client(github)
                .map(UserService::new)
                .orElseThrow();
        return us.getUser();
    }

    private void delete(GitHub github) throws IOException {
        if (github.getRepository() != null) {
            log.info("Deleting GitHub repository: {}", github.getSlug());
            this.repositoryService(github).deleteRepository(github.getRepository());
        }
    }

    // -- GIT --

    private void commitSources(Project project, final String message) {
        log.info("Committing sources to {}", project.getRepository().getSlug());
        Git git = project.getRepository().getGit();
        try (final Stream<Path> sources = Files.list(project.getDirectory())) {
            // Adds files to the index
            addFile(git, sources);

            // commits
            final RevCommit commit = commit(git, message);
            log.info("= {}", commit);

            // pushes
            this.push(project.getRepository(), git);
        } catch (Exception e) {
            throw new InitializerException(e,
                    "Failed to commit sources on %s from: %s",
                    project.getRepository().getSlug(), project.getDirectory());
        }
    }

    private void addFile(Git git, Stream<Path> sources) {
        sources.map(Path::getFileName)
                .map(Path::toString)
                .filter(fn -> !".git".equals(fn))
                .forEach(fn -> {
                    try {
                        git.add().addFilepattern(fn).call();
                        log.debug("./{} added", fn);
                    } catch (GitAPIException e) {
                        throw new InitializerException("Git addFile failed for file name: %s", fn);
                    }
                });
    }

    private Git cloneRepository(Project project, Repository repository) throws GitAPIException {
        return Git.cloneRepository()
            .setURI(repository.getCloneUrl())
            .setCredentialsProvider(credentials(repository))
            .setDirectory(project.getDirectory().toFile())
            .call();
    }

    private RevCommit commit(Git git, String message) throws GitAPIException {
        return git
            .commit()
            .setMessage(message)
            .call();
    }

    private void push(Repository repository, Git git) throws GitAPIException {
        git.push()
            .setCredentialsProvider(credentials(repository))
            .call()
            .forEach(pr -> log.info("= push: {}", pr.getMessages()));
    }

    private UsernamePasswordCredentialsProvider credentials(Repository repository) {
        return hasText(repository.getUsername()) && hasText(repository.getPassword())
            ? new UsernamePasswordCredentialsProvider(repository.getUsername(), repository.getPassword())
            : new UsernamePasswordCredentialsProvider(repository.getToken(), "");
    }

}
