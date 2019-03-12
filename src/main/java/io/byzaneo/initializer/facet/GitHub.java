package io.byzaneo.initializer.facet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import static io.byzaneo.initializer.Constants.FacetFamily.Repository;

@Data
@EqualsAndHashCode(callSuper = true)
@Component(GitHub.FACET_NAME)
public class GitHub extends Facet {

    public static final String FACET_NAME = "GitHub";

    private String token;

    public GitHub() {
        super(Repository, FACET_NAME, "https://github.com/");
    }

}
