package io.byzaneo.initializer;

import io.byzaneo.initializer.bean.Project;
import io.byzaneo.initializer.data.ProjectRepository;
import io.byzaneo.initializer.service.InitializerService;
import io.byzaneo.one.test.ReactiveMongoCleanupRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static io.byzaneo.one.Constants.PROFILE_TEST;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(PROFILE_TEST)
public class InitializerServiceTest {

    @Autowired
    private InitializerService service;
    @Autowired
    private ProjectRepository repo;
    @Autowired
    private ReactiveMongoTemplate mongoTemplate;

    @Rule
    public final ReactiveMongoCleanupRule cleanupRule = new ReactiveMongoCleanupRule(this, Project.class);

    @Test
    public void create() {
        this.service.create(Project.builder()
                .name("test")
                .owner("tester")
                .organization("Byzaneo")
                .build());
    }

}
