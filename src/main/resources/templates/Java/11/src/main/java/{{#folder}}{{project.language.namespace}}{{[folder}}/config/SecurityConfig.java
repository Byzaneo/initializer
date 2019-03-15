package {{project.language.namespace}};

import io.byzaneo.one.config.AbstractSecurityConfig;
import io.byzaneo.one.config.ByzaneoProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends AbstractSecurityConfig {

    protected SecurityConfig(ByzaneoProperties properties) {
        super(properties);
    }

    @Override
    protected void authorizeRequests(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry requests) {
        requests
            .antMatchers("/api/**").hasAuthority("{{project.name}}");
    }

}
