package {{project.language.namespace}};

import io.byzaneo.one.billing.Price;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

{{project.language.namespace}};

@RestController
@RequestMapping("/api")
public class {{capitalizedName}}Controller {

    @Price(value = "0.01", description = "Example of priced endpoint")
    @GetMapping(path = "/info")
    public ResponseEntity<String> info() {
        try {
            return ok("I'm the {{project.language.namespace}} service.");
        } catch (Exception e) {
            return status(BAD_REQUEST).body(e.getMessage());
        }
    }
}
