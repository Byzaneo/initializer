package io.byzaneo.initializer.service;

import io.byzaneo.initializer.bean.Project;
import io.byzaneo.initializer.event.ProjectIntegrationEvent;
import io.byzaneo.initializer.event.ProjectPostEvent;
import io.byzaneo.initializer.event.ProjectPreEvent;
import io.byzaneo.initializer.event.ProjectRepositoryEvent;
import io.byzaneo.initializer.facet.Docker;
import io.byzaneo.initializer.facet.Java;
import io.byzaneo.initializer.service.SourcesService.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

import static io.byzaneo.initializer.service.SourcesService.EL;
import static io.byzaneo.initializer.service.SourcesService.EL_CONTEXT;
import static io.byzaneo.one.Constants.PROFILE_TEST;
import static java.nio.file.Files.*;
import static java.util.Comparator.reverseOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(PROFILE_TEST)
public class TravisServiceTest {

    @Autowired
    private SourcesService sourcesService;
    @Autowired
    private GitHubService githubService;
    @Autowired
    private TravisService service;
    @Value("${initializer.github.organization}")
    private String organization;


    private Project project;

    @Before
    public void before() throws IOException {
        final Path dir = Paths.get("C:\\Temp\\travis");
        if ( isDirectory(dir) )
            walk(dir)
                .sorted(reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);

        final String name = "test";
        this.project = Project.builder()
                .name(name)
                .namespace("io.byzaneo")
                .ownerName("Tester")
                .owner("tester@byzaneo.io")
                .registry(new Docker("my.registry", "me", "secret"))
                .directory(dir)
                .build();

        // init default values
        final ProjectPreEvent event = new ProjectPreEvent(project);
        this.githubService.onInit(event);
        this.service.onInit(event);
    }

    @Test
    public void el() {
        assertEquals(
                "jdk: openjdk"+((Java)project.getLanguage()).getVersion(),
                EL.parseExpression("language.id eq 'Java' ?  'jdk: openjdk' + language.version : ''").getValue(EL_CONTEXT, project, String.class));
    }

    @Test
    public void sources() throws IOException {
        this.sourcesService.generateSources(new Context(project), "Travis");

        final AtomicInteger count = new AtomicInteger();
        walk(project.getDirectory())
                .peek(System.out::println)
                .forEach(p -> {count.incrementAndGet(); assertTrue(exists(p));});
        assertEquals(3, count.get());
    }

    @Test
    public void deactivate() {
        this.service.onRepositoryCreated(new ProjectRepositoryEvent(project));
    }

    @Test
    public void activate() {
        this.service.onProjectCreated(new ProjectPostEvent(project));
    }

    @Test
    public void env() {
        this.service.onCreateIntegration(new ProjectIntegrationEvent(project));
    }
}
