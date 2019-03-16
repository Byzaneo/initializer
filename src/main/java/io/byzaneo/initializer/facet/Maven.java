package io.byzaneo.initializer.facet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotBlank;

import static io.byzaneo.initializer.Constants.FacetFamily.Management;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Data
@EqualsAndHashCode(callSuper = true)
@Component(Maven.FACET_NAME)
@Scope(SCOPE_PROTOTYPE)
public class Maven extends Facet {

    public static final String FACET_NAME = "Maven";

    @NotBlank
    private String version = "1.0.0-SNAPSHOT";

    public Maven() {
        super(Management, FACET_NAME, "https://maven.apache.org/");
    }

    @Override
    public String getTemplatesLocation() {
        return FACET_NAME;
    }
}