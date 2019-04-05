package io.byzaneo.initializer.facet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.egit.github.core.Repository;
import org.springframework.context.annotation.Scope;
import org.springframework.data.annotation.Transient;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;
import static org.springframework.util.StringUtils.hasText;

@Data
@EqualsAndHashCode(callSuper = true)
@Component(GitHub.FACET_ID)
@Scope(SCOPE_PROTOTYPE)
public class GitHub extends io.byzaneo.initializer.facet.Repository {

    public static final String FACET_ID = "GitHub";

    @Transient
    private Repository repository;

    public GitHub() {
        super(FACET_ID, "https://github.com/");
    }

    public GitHub(String username) {
        this();
        this.username = username;
    }

    public GitHub(String organization, String name) {
        this();
        this.organization = organization;
        this.name = name;
    }

    @Override
    public String getSlug() {
        Assert.hasText(name, "Repository name is required");

        String slug = "";
        if ( hasText(organization) )
            slug += organization;
        else if ( hasText(username) )
            slug += username;
        else
            throw new IllegalStateException("Repository requires an username or an organization");

        return slug.concat("/").concat(name);
    }
}
