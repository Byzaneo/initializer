package io.byzaneo.initializer.facet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static io.byzaneo.initializer.Constants.FacetFamily.Coverage;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Data
@EqualsAndHashCode(callSuper = true)
@Component(CodeCov.FACET_ID)
@Scope(SCOPE_PROTOTYPE)
public class CodeCov extends Facet {

    public static final String FACET_ID = "CodeCov";

    public CodeCov() {
        super(Coverage, FACET_ID, "https://codecov.io");
    }

}
