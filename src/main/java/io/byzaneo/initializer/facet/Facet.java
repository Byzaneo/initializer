package io.byzaneo.initializer.facet;

import io.byzaneo.initializer.Constants;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Transient;

import javax.validation.constraints.NotNull;

import static lombok.AccessLevel.NONE;

@Data
@RequiredArgsConstructor
public abstract class Facet {

    @NotNull
    @NonNull
    @Setter(NONE)
    @Transient
    protected Constants.FacetFamily family;

    @NotNull
    @NonNull
    @Setter(NONE)
    @Transient
    protected String name;

    @NotNull
    @NonNull
    protected String home;

}
