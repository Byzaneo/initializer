package {{project.language.namespace}};

import io.byzaneo.one.Boots;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class {{capitalizedName}}App {

    public static void main(String[] args) {
        Boots.run({{capitalizedName}}App.class, args);
    }
}
