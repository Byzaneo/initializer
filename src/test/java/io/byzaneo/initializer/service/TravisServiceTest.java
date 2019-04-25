package io.byzaneo.initializer.service;

import io.byzaneo.initializer.bean.Project;
import io.byzaneo.initializer.event.ProjectIntegrationEvent;
import io.byzaneo.initializer.event.ProjectPostEvent;
import io.byzaneo.initializer.event.ProjectPreEvent;
import io.byzaneo.initializer.event.ProjectRepositoryEvent;
import io.byzaneo.initializer.facet.Java;
import io.byzaneo.initializer.facet.Travis;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static io.byzaneo.initializer.Tests.project;
import static io.byzaneo.initializer.service.SourcesService.EL;
import static io.byzaneo.initializer.service.SourcesService.EL_CONTEXT;
import static io.byzaneo.one.Constants.PROFILE_TEST;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(PROFILE_TEST)
public class TravisServiceTest {

    @Autowired
    private SourcesService sourcesService;
    @Autowired
    private TravisService service;
    @Value("${initializer.github.organization}")
    private String organization;


    private Project project;

    @Before
    public void before() {
        // needs project git repository created
        this.project = project("travis").build();
        this.service.onInit(new ProjectPreEvent(this.project));
    }

    @Test
    public void el() {
        assertEquals(
                "jdk: openjdk"+((Java)project.getLanguage()).getVersion(),
                EL.parseExpression("language.id eq 'Java' ?  'jdk: openjdk' + language.version : ''").getValue(EL_CONTEXT, project, String.class));
    }

    @Test
    public void sources() {
        final String travis = this.sourcesService.transform(project, Travis.FACET_ID + "/.travis.yml");
        //System.out.println(travis);
        assertTrue(travis.startsWith("language: java\r\njdk: openjdk12"));
        assertTrue(travis.contains("- docker"));
        assertTrue(travis.contains("- mvn package -U -B -P prod,coverage"));
        assertTrue(travis.contains("- bash <(curl -s https://codecov.io/bash)"));
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
