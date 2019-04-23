package io.byzaneo.initializer.facet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static io.byzaneo.initializer.Constants.FacetFamily.Deployment;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Data
@EqualsAndHashCode(callSuper = true)
@Component(Spinnaker.FACET_ID)
@Scope(SCOPE_PROTOTYPE)
public class Spinnaker extends Facet {

    public static final String FACET_ID = "Spinnaker";

    private String api;

    public Spinnaker() {
        super(Deployment, FACET_ID, "https://spinnaker.io/");
    }

    @Override
    public String getTemplatesLocation() {
        return FACET_ID;
    }
}
