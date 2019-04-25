package {{project.namespace}}.{{project.name}}.rest;

import io.byzaneo.one.test.WithMockToken;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import static io.byzaneo.one.Constants.PROFILE_TEST;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(PROFILE_TEST)
public class {{#capitalize}}{{project.name}}{{/capitalize}}ControllerTest {

    @Autowired
    ApplicationContext context;

    private WebTestClient client;

    @Before
    public void setUp() {
        this.client = WebTestClient
                .bindToApplicationContext(this.context)
                .configureClient()
                .build();
    }

    @Test
    public void testInfo() {
        this.client.get()
                .uri({{#capitalize}}{{project.name}}{{/capitalize}}Controller.BASE_PATH)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Hello! I'm the {{project.name}} service.");
    }

    @Test
    @WithMockToken(scopes = "{{project.name}}")
    public void testSecure() {
        this.client.get()
                .uri({{#capitalize}}{{project.name}}{{/capitalize}}Controller.BASE_PATH + "/secure")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Hello "+WithMockToken.TESTER_EMAIL);
    }
}
