package io.byzaneo.initializer.service;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import io.byzaneo.initializer.bean.Project;
import io.byzaneo.initializer.event.ProjectSourcesEvent;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import static java.nio.file.Files.exists;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SourcesServiceTest {

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

        final Project project = Project.builder()
                .name("test")
                .namespace("io.byzaneo")
                .ownerName("Tester")
                .owner("tester@byzaneo.io")
                .build();

        new SourcesService().onCreateSources(new ProjectSourcesEvent(project));

        Files.walk(project.getDirectory())
                .peek(System.out::println)
                .forEach(p -> assertTrue(exists(p)));
    }

}
