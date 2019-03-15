package {{project.namespace}}.{{project.name}};

import io.byzaneo.one.Boots;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class {{#capitalize}}{{project.name}}{{/capitalize}}App {

    public static void main(String[] args) {
        Boots.run({{#capitalize}}{{project.name}}{{/capitalize}}App.class, args);
    }
}
