package io.byzaneo.initializer;

import io.byzaneo.one.Boots;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class InitializerApp {

    public static void main(String[] args) {
        Boots.run(InitializerApp.class, args);
    }
}
