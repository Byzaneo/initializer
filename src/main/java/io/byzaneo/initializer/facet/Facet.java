package io.byzaneo.initializer.facet;

import io.byzaneo.initializer.Constants;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Transient;

import javax.validation.constraints.NotNull;

@Data
@RequiredArgsConstructor
public abstract class Facet {

    @NonNull
    @Transient
    protected final Constants.FacetFamily family;

    @NonNull
    @Transient
    protected final String name;

    @NotNull
    @NonNull
    protected String home;

    /**
     * @return the classpath location of this facet sources
     *      templates within the classpath:/templates package.
     *      Returns null, if this facet has not templates.
     */
    public String getTemplatesLocation() {
        return null;
    }
}
