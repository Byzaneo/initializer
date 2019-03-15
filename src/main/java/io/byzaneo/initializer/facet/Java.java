package io.byzaneo.initializer.facet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotBlank;

import static io.byzaneo.initializer.Constants.FacetFamily.Language;

@Data
@EqualsAndHashCode(callSuper = true)
@Component(Java.FACET_NAME)
public class Java extends Facet {

    public static final String FACET_NAME = "Java";

    @NotBlank
    private String version = "11";

    public Java() {
        super(Language, FACET_NAME, "https://openjdk.java.net/");
    }

    public Java(@NotBlank String version) {
        this();
        this.version = version;
    }

    @Override
    public String getTemplatesLocation() {
        return FACET_NAME + "/" + this.version;
    }
}
