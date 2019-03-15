package io.byzaneo.initializer.facet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotBlank;

import static io.byzaneo.initializer.Constants.FacetFamily.Management;

@Data
@EqualsAndHashCode(callSuper = true)
@Component(Maven.FACET_NAME)
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
