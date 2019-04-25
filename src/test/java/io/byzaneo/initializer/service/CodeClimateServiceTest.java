package io.byzaneo.initializer.service;

import io.byzaneo.initializer.Tests;
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
    private CodeClimateService service;

    private Project project;

    @Before
    public void before() throws Exception {
        // needs project git repository created
        this.project = Tests.project().build();
        this.service.onInit(new ProjectPreEvent(project));
    }

    @Test
    public void activate() {
        this.service.onActivate(new ProjectIntegrationEvent(project));
    }
}
