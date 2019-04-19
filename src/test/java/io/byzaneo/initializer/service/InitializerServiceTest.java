package io.byzaneo.initializer.service;

import io.byzaneo.initializer.bean.Project;
import io.byzaneo.initializer.data.ProjectRepository;
import io.byzaneo.one.test.ReactiveMongoCleanupRule;
import io.byzaneo.one.test.WithMockToken;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.NoSuchElementException;

import static io.byzaneo.one.Constants.PROFILE_TEST;
import static reactor.core.publisher.Mono.defer;
import static reactor.core.publisher.Mono.error;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(PROFILE_TEST)
public class InitializerServiceTest {

    @Autowired
    private InitializerService service;
    @Autowired
    @SuppressWarnings("unused")
    private GitHubService github;
    @Autowired
    @SuppressWarnings("unused")
    private ProjectRepository repo;
    @Autowired
    @SuppressWarnings("unused")
    private ReactiveMongoTemplate mongoTemplate;

    @Rule
    public final ReactiveMongoCleanupRule cleanupRule = new ReactiveMongoCleanupRule(this, Project.class);

    private Project project = Project.builder()
            .name("dummy")
            .namespace("io.byzaneo")
            .build();

    @Test
    @WithMockToken
    public void test() {
        this.service
                .create(project)
                .switchIfEmpty(defer(() -> error(new NoSuchElementException("No value present"))))
                .then(this.service.delete(project))
                .block();
    }

}
