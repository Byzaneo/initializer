package io.byzaneo.initializer.facet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.byzaneo.initializer.Constants;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Transient;

import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
@RequiredArgsConstructor
public abstract class Facet {
    private static final ObjectMapper MAPPER = new ObjectMapper();

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
    @JsonIgnore
    public String getTemplatesLocation() {
        return null;
    }

    public Map<String, Object> toProperties() {
        final Map<String, Object> props =
                MAPPER.convertValue(this, new TypeReference<Map<String, String>>(){});
        props.remove("family");
        props.remove("id");
        props.remove("home");
        return props;
    }
}
