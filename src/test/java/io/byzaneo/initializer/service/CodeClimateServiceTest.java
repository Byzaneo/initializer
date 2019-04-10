package io.byzaneo.initializer.service;

import io.byzaneo.initializer.bean.Project;
import io.byzaneo.initializer.event.ProjectIntegrationEvent;
import io.byzaneo.initializer.event.ProjectPreEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static io.byzaneo.one.Constants.PROFILE_TEST;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(PROFILE_TEST)
public class CodeClimateServiceTest {

    @Autowired
    private GitHubService githubService;
    @Autowired
    private CodeClimateService service;

    private Project project;

    @Before
    public void before() throws Exception {
        this.project = Project.builder()
                .name("dummy")
                .namespace("io.byzaneo")
                .registry(null)
                .coverage(null)
                .build();

        // init default values
        final ProjectPreEvent event = new ProjectPreEvent(project);
        this.githubService.onInit(event);
        this.service.onInit(event);
    }

    @Test
    public void activate() {
        this.service.onInit(new ProjectIntegrationEvent(project));
    }
}
