package io.byzaneo.initializer.facet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotBlank;

import static io.byzaneo.initializer.Constants.FacetFamily.Language;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Data
@EqualsAndHashCode(callSuper = true)
@Component(Java.FACET_ID)
@Scope(SCOPE_PROTOTYPE)
public class Java extends Facet {

    public static final String FACET_ID = "Java";

    @NotBlank
    private String version = "12";

    public Java() {
        super(Language, FACET_ID, "https://openjdk.java.net/");
    }

    public Java(@NotBlank String version) {
        this();
        this.version = version;
    }

    @Override
    public String getTemplatesLocation() {
        return FACET_ID + "/" + this.version;
    }
}
