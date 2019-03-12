package io.byzaneo.initializer;

import io.byzaneo.initializer.bean.Project;
import io.byzaneo.initializer.service.InitializerService;
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
public class InitializerServiceTest {

    @Autowired
    private InitializerService service;

    @Test
    public void create() {
        this.service.create(Project.builder()
                .name("test")
                .owner("tester")
                .organization("Byzaneo")
                .build());
    }

}
