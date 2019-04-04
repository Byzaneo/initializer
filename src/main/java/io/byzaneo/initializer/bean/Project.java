package io.byzaneo.initializer.bean;

import io.byzaneo.initializer.Constants.Mode;
import io.byzaneo.initializer.facet.*;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static java.nio.file.Files.createTempDirectory;
import static java.util.stream.Stream.of;
import static lombok.AccessLevel.NONE;

/**
 * TODO license management https://developer.github.com/v3/licenses/#get-an-individual-license
 * TODO README
 */
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = Project.COLLECTION)
public class Project {

    public static final String COLLECTION = "projects";

    @Id
    private String id;
    @Builder.Default
    private Instant date = Instant.now();
    @NonNull
    @Indexed(unique = true)
    @NotBlank
    @Pattern(regexp = "^[a-z][a-z0-9_]*$")
    private String name;
    @NonNull
    @NotBlank
    @Pattern(regexp = "^[a-z][a-z0-9_]*(\\.[a-z0-9_]+)+[0-9a-z_]$")
    private String namespace;
    private String description;

    // - Security -

    private String owner;
    @Field("owner_name")
    private String ownerName;

    // - Facets -

    @NotNull
    @Builder.Default
    public Facet language = new Java();
    @Builder.Default
    public Facet repository = new GitHub();
    @Builder.Default
    public Facet management = new Maven();
    @Builder.Default
    public Facet assembly = new Docker();
//    @Builder.Default
    public Facet registry; // = "Nexus";
    @Builder.Default
    public Facet integration = new Travis();
    @Builder.Default
    public Facet coverage = new CodeCov();
//    @Builder.Default
    public Facet quality; // = "CodeClimate";
//    @Builder.Default
    public Facet deployment; // = "Spinnaker";
//    @Builder.Default
    public Facet front;

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
            return directory==null
                    ? directory = createTempDirectory(this.name)
                    : directory;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Stream<Facet> facets() {
        return of(language, repository, management, assembly, registry,
                integration, coverage, quality, deployment, front)
                .filter(Objects::nonNull);
    }
}
