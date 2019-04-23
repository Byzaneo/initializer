package io.byzaneo.initializer.facet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static io.byzaneo.initializer.Constants.FacetFamily.Registry;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Data
@EqualsAndHashCode(callSuper = true)
@Component(Docker.FACET_ID)
@Scope(SCOPE_PROTOTYPE)
public class Docker extends Facet {

    public static final String FACET_ID = "Docker";

    private String hostname;
    private String username;
    private String password;

    public Docker() {
        super(Registry, FACET_ID, "https://www.docker.com/");
    }

    public Docker(String hostname, String username, String password) {
        this();
        this.hostname = hostname;
        this.username = username;
        this.password = password;
    }

    @Override
    public String getTemplatesLocation() {
        return FACET_ID;
    }
}
