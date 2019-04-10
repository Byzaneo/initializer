package io.byzaneo.initializer.facet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static io.byzaneo.initializer.Constants.FacetFamily.Quality;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Data
@EqualsAndHashCode(callSuper = true)
@Component(CodeClimate.FACET_ID)
@Scope(SCOPE_PROTOTYPE)
public class CodeClimate extends Facet {

    public static final String FACET_ID = "CodeClimate";

    private String token;
    private String api;

    public CodeClimate() {
        super(Quality, FACET_ID, "https://codeclimate.com/");
    }
}
