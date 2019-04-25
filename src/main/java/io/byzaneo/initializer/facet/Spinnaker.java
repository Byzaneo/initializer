package io.byzaneo.initializer.facet;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

import static io.byzaneo.initializer.Constants.FacetFamily.Deployment;
import static io.byzaneo.initializer.service.SpinnakerService.DEFAULT_PIPELINE;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Data
@EqualsAndHashCode(callSuper = true)
@Component(Spinnaker.FACET_ID)
@Scope(SCOPE_PROTOTYPE)
public class Spinnaker extends Facet {

    public static final String FACET_ID = "Spinnaker";

    private String api;
    private String account;
    @NotNull
    @NonNull
    @Builder.Default
    private String pipeline = DEFAULT_PIPELINE;

    public Spinnaker() {
        super(Deployment, FACET_ID, "https://spinnaker.io/");
    }

    public Spinnaker(String api, String account) {
        this();
        this.api = api;
        this.account = account;
    }
}
