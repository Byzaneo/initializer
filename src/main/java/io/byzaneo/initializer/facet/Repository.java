package io.byzaneo.initializer.facet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.byzaneo.initializer.Constants.FacetFamily;
import io.byzaneo.initializer.bean.Project;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.jgit.api.Git;
import org.springframework.data.annotation.Transient;

/**
 * Repository facet
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class Repository extends Facet {

    /** Account (if null: resolved by token) */
    protected String username;
    /** Password */
    protected String password;
    /** API Access token */
    protected String token;
    /** Repository name (default {@link Project#getName()} */
    protected String name;
    /** Group within the repository stands: GitHub organization, BitBucket project... */
    protected String organization;

    /** Local git repository */
    @Transient
    @JsonIgnore
    private Git git;

    public Repository(String name, String home) {
        super(FacetFamily.Repository, name, home);
    }

    /**
     * @return the URL-friendly version of a repository name
     */
    @JsonIgnore
    public abstract String getSlug();

    /**
     * @return the clone URL of this repository
     */
    public abstract String getCloneUrl();
}
