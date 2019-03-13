package io.byzaneo.initializer.bean;

import io.byzaneo.initializer.Constants.Mode;
import io.byzaneo.initializer.facet.Facet;
import io.byzaneo.initializer.facet.GitHub;
import io.byzaneo.initializer.facet.Java;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;

import static java.nio.file.Files.createTempDirectory;
import static java.util.Optional.ofNullable;
import static lombok.AccessLevel.NONE;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = Project.COLLECTION)
@CompoundIndexes({
        @CompoundIndex(name = "name_unique", unique = true, def = "{'name' : 1, 'owner' : 1}")
})
public class Project {

    public static final String COLLECTION = "projects";

    @Id
    private String id;
    @Builder.Default
    private Instant date = Instant.now();
    @NonNull
    @Indexed
    @Pattern(regexp = "^[a-z][a-z0-9_]*$")
    private String name;

    // - Security -

    @NonNull
    private String owner;
    @NotBlank
    @NonNull
    private String organization;


    // - Facets -

    @NotNull
    @Getter(NONE)
    @Builder.Default
    public Facet language = new Java();
    @Getter(NONE)
    @Builder.Default
    public Facet repository = new GitHub();
//    public String management = "Maven";
//    public String assembly = "Docker";
//    public String registry = "Nexus";
//    public String integration = "Travis";
//    public String coverage = "CodeCov";
//    public String quality = "CodeClimate";
//    public String deployment = "Spinnaker";
//    public String front;

    // - Transient -

    @Transient
    @Builder.Default
    private Mode mode = Mode.create;
    @Transient
    @Getter(NONE)
    private Path directory;
    @Transient
    @Singular
    private Map<String, String> properties;

    public Path getDirectory() {
        try {
            return ofNullable(directory)
                    .orElse(directory = createTempDirectory(this.name));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Facet> T getLanguage() {
        return (T)language;
    }

    @SuppressWarnings("unchecked")
    public <T extends Facet> T getRepository() {
        return (T)repository;
    }
}
