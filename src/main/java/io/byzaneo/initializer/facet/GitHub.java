package io.byzaneo.initializer.facet;

import io.byzaneo.initializer.Constants.FacetFamily;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.jgit.api.Git;
import org.springframework.context.annotation.Scope;
import org.springframework.data.annotation.Transient;
import org.springframework.stereotype.Component;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Data
@EqualsAndHashCode(callSuper = true)
@Component(GitHub.FACET_NAME)
@Scope(SCOPE_PROTOTYPE)
public class GitHub extends Facet {

    public static final String FACET_NAME = "GitHub";

    private String token;
    private String organization;

    @Transient
    private Repository repository;
    @Transient
    private Git git;

    public GitHub() {
        super(FacetFamily.Repository, FACET_NAME, "https://github.com/");
    }
}
