package io.byzaneo.initializer.facet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static io.byzaneo.initializer.Constants.FacetFamily.Assembly;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Data
@EqualsAndHashCode(callSuper = true)
@Component(Docker.FACET_NAME)
@Scope(SCOPE_PROTOTYPE)
public class Docker extends Facet {

    public static final String FACET_NAME = "Docker";

    private String registry;
    private String username;
    private String password;

    public Docker() {
        super(Assembly, FACET_NAME, "https://www.docker.com/");
    }

    @Override
    public String getTemplatesLocation() {
        return FACET_NAME;
    }
}
