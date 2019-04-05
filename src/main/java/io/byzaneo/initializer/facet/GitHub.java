package io.byzaneo.initializer.facet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.egit.github.core.Repository;
import org.springframework.context.annotation.Scope;
import org.springframework.data.annotation.Transient;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.validation.constraints.NotBlank;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;
import static org.springframework.util.StringUtils.hasText;

@Data
@EqualsAndHashCode(callSuper = true)
@Component(GitHub.FACET_ID)
@Scope(SCOPE_PROTOTYPE)
public class GitHub extends io.byzaneo.initializer.facet.Repository {

    public static final String FACET_ID = "GitHub";

    @Transient
    @JsonIgnore
    private Repository repository;

    public GitHub() {
        super(FACET_ID, "https://github.com/");
    }

    public GitHub(@NotBlank String username, String password, @NotBlank String name) {
        this();
        this.username = username;
        this.password = password;
        this.name = name;
    }

    public GitHub(@NotBlank String organization, @NotBlank String name) {
        this();
        this.organization = organization;
        this.name = name;
    }

    @Override
    public String getSlug() {
        Assert.hasText(name, "GitHub repository name is required");
        Assert.isTrue(hasText(organization) || hasText(username), "GitHub repository organization or username is required");
        return (hasText(organization) ? organization : username) + "/" + name;
    }
}
