package io.byzaneo.initializer.service;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import io.byzaneo.initializer.bean.Project;
import io.byzaneo.initializer.event.ProjectSourcesEvent;
import io.byzaneo.initializer.facet.Docker;
import io.byzaneo.initializer.facet.GitHub;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static java.nio.file.Files.*;
import static java.util.Comparator.reverseOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SourcesServiceTest {

    private Project project;
    private final SourcesService service = new SourcesService();

    @Before
    public void before() throws IOException {
        final Path dir = Paths.get("C:\\Temp\\sources");
        if ( isDirectory(dir) )
            walk(dir)
                .sorted(reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);

        project = Project.builder()
                .name("dummy")
                .namespace("io.byzaneo")
                .ownerName("Tester")
                .owner("tester@byzaneo.io")
                .repository(new GitHub("my-org", "my-repo"))
                .registry(new Docker("my.registry", "me", "secret"))
                .directory(dir)
                .build();
    }

    @Test
    public void simple() {
        String text = "One, two, {{three}}. Three sir!";
        Template tmpl = Mustache.compiler().compile(text);
        assertEquals(
                "One, two, five. Three sir!",
                tmpl.execute(Map.of("three", "five")));
    }

    @Test
    public void create() throws IOException {
        service.onCreateSources(new ProjectSourcesEvent(project));

        walk(project.getDirectory())
                .peek(System.out::println)
                .forEach(p -> assertTrue(exists(p)));
    }

}
