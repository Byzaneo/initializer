package {{project.namespace}}.{{project.name}}.rest;

import io.byzaneo.one.billing.Price;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
@RequestMapping("/api")
public class {{#capitalize}}{{project.name}}{{/capitalize}}Controller {

    @Price(value = "0.01", description = "Example of priced endpoint")
    @GetMapping
    public ResponseEntity<String> info() {
        try {
            return ok("Hello! I'm the {{project.name}} service.");
        } catch (Exception e) {
            return status(BAD_REQUEST).body(e.getMessage());
        }
    }
}
