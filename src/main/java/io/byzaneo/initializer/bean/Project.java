package io.byzaneo.initializer.bean;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static lombok.AccessLevel.NONE;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = Project.COLLECTION)
public class Project {

    public static final String COLLECTION = "projects";

    @Id
    private String id;

    // - General -
    @NonNull
    @NotBlank
    private String name;

    // - Security -
    @NonNull
    private String owner;
    @NotBlank
    @NonNull
    private String organization;

    // - Facets -
    @Singular
    private Map<String, String> facets;

    // - Working Directory -
    @Transient
    @Getter(NONE)
    private Path workDir;

//    public language,
//    management,
//    assembly,
//    registry,
//    integration,
//    coverage,
//    quality,
//    deployment,
//    ui,
//    repository


    public Path getWorkDir() {
        try {
            return workDir==null
                    ? Files.createTempDirectory(this.name)
                    : workDir;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
