package io.byzaneo.initializer;

import io.byzaneo.initializer.bean.Project;
import io.byzaneo.initializer.facet.Docker;
import io.byzaneo.initializer.facet.GitHub;
import io.byzaneo.initializer.facet.Spinnaker;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.byzaneo.one.test.WithMockToken.TESTER_EMAIL;
import static io.byzaneo.one.test.WithMockToken.TESTER_NAME;
import static java.lang.System.getProperty;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.walk;
import static java.util.Comparator.reverseOrder;

public abstract class Tests {

    public static final String PROJECT_NAME = "dummy";

    public static Project.ProjectBuilder project() {
        return project("dummy", null);
    }

    public static Project.ProjectBuilder project(String directory) {
        return project(PROJECT_NAME, directory);
    }

    public static Project.ProjectBuilder project(String name, String directory) {
        return Project.builder()
            .name(name)
            .port(8080)
            .namespace("io.byzaneo")
            .ownerName(TESTER_NAME)
            .owner(TESTER_EMAIL)
            .directory(directory(directory))
            .repository(new GitHub("Byzaneo", PROJECT_NAME))
            .registry(new Docker("my.registry", "me", "secret", "local-registry"))
//            .integration(new Travis())
//            .coverage(new CodeCov())
//            .quality(new CodeClimate())
//            .integration(new Travis())
            .deployment(new Spinnaker("spin-api", "spin-account"));
    }

    private static Path directory(String name) {
        if ( name==null )
            return null;

        final Path dir = Paths.get(getProperty("java.io.tmpdir"),name);
        if ( isDirectory(dir) ) {
            try {
                walk(dir)
                        .sorted(reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return dir;
    }
}
