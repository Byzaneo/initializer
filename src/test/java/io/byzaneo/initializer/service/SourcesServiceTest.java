package io.byzaneo.initializer.service;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import io.byzaneo.initializer.bean.Project;
import io.byzaneo.initializer.event.ProjectSourcesEvent;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static io.byzaneo.initializer.Tests.project;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.walk;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SourcesServiceTest {

    private Project project;
    private final SourcesService service = new SourcesService();

    @Before
    public void before() {
        project = project("sources").build();
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
