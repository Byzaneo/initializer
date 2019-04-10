package {{project.namespace}}.{{project.name}}.rest;

import io.byzaneo.one.billing.Price;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static io.byzaneo.dummy.rest.{{#capitalize}}{{project.name}}{{/capitalize}}Controller.BASE_PATH;
import static io.byzaneo.one.SecurityContext.userEmail;

@RestController
@RequestMapping(BASE_PATH)
public class {{#capitalize}}{{project.name}}{{/capitalize}}Controller {

    public static final String BASE_PATH = "/api";

    @Price(value = "0.01", description = "Example of priced endpoint")
    @GetMapping
    public Mono<String> info() {
        return Mono.just("Hello! I'm the {{project.name}} service.");
    }

    @GetMapping("/secure")
    public Mono<String> secure() {
        return userEmail().map("Hello "::concat);
    }
}
