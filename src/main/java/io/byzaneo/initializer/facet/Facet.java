package io.byzaneo.initializer.facet;

import io.byzaneo.initializer.Constants;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.data.annotation.Transient;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@Data
@RequiredArgsConstructor
public abstract class Facet {

    @NonNull
    @Transient
    protected final Constants.FacetFamily family;

    @NonNull
    @Transient
    protected final String id;

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

    @SuppressWarnings("unchecked")
    public Map<String, Object> toMap() {
        final Map<String, Object> props = new HashMap<>(BeanMap.create(this));

        props.remove("family");
        props.remove("id");
        props.remove("home");
        props.remove("templatesLocation");

        return props;
    }
}
