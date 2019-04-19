package io.byzaneo.initializer.config;

import io.byzaneo.one.config.AbstractSecurityConfig;
import io.byzaneo.one.config.ByzaneoProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig extends AbstractSecurityConfig {

    protected SecurityConfig(ByzaneoProperties properties) {
        super(properties);
    }

    @Override
    protected ServerHttpSecurity.AuthorizeExchangeSpec authorizeExchange(ServerHttpSecurity.AuthorizeExchangeSpec exchanges) {
        return exchanges
                .pathMatchers("/api/**").hasAuthority("initializer");
    }

}
