package io.byzaneo.initializer.facet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

import static io.byzaneo.initializer.Constants.FacetFamily.Language;

@Data
@EqualsAndHashCode(callSuper = true)
@Component(Java.FACET_NAME)
public class Java extends Facet {

    public static final String FACET_NAME = "Java";

    /** package */
    private String namespace;
    private String version = "11";

    public Java() {
        super(Language, FACET_NAME, "https://openjdk.java.net/");
    }

    public Java(String namespace, @NotNull String version) {
        this();
        this.namespace = namespace;
        this.version = version;
    }

    @Override
    public String getTemplatesLocation() {
        return FACET_NAME + "/" + this.version;
    }
}
