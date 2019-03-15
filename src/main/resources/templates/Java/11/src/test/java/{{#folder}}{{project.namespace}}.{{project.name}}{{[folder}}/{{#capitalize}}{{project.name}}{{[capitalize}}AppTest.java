package {{project.namespace}}.{{project.name}}.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static io.byzaneo.one.Constants.PROFILE_TEST;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(PROFILE_TEST)
public class {{#capitalize}}{{project.name}}{{/capitalize}}AppTest {

    @Test
    public void contextLoads() {
        // simple application loading test
    }

}
