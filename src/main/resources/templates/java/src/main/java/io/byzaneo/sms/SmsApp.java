package io.byzaneo.initializer;

import io.byzaneo.one.Boots;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class SmsApp {

    public static void main(String[] args) {
        Boots.run(SmsApp.class, args);
    }
}
