package io.byzaneo.initializer.facet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static io.byzaneo.initializer.Constants.FacetFamily.Integration;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Data
@EqualsAndHashCode(callSuper = true)
@Component(Travis.FACET_ID)
@Scope(SCOPE_PROTOTYPE)
public class Travis extends Facet {

    public static final String FACET_ID = "Travis";

    private String token;
    private String api;

    public Travis() {
        super(Integration, FACET_ID, "https://travis-ci.com");
    }

    @Override
    public String getTemplatesLocation() {
        return FACET_ID;
    }
}
