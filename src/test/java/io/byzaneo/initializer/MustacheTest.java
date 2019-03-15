package io.byzaneo.initializer;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import io.byzaneo.initializer.bean.Project;
import io.byzaneo.initializer.event.ProjectSourcesEvent;
import io.byzaneo.initializer.facet.Java;
import io.byzaneo.initializer.service.SourcesService;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import static java.nio.file.Files.exists;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MustacheTest {

    @Test
    public void simple() {
        String text = "One, two, {{three}}. Three sir!";
        Template tmpl = Mustache.compiler().compile(text);
        assertEquals(
                "One, two, five. Three sir!",
                tmpl.execute(Map.of("three", "five")));
    }

    @Test
    public void projectContext() throws IOException {

        final Project project = Project.builder()
                .name("test")
                .namespace("io.byzaneo")
                .owner("tester@byzaneo.io")
                .organization("byzaneo")
                .language(new Java("11"))
                .build();

        new SourcesService().onCreateSources(new ProjectSourcesEvent(project));

        Files.walk(project.getDirectory())
                .peek(System.out::println)
                .forEach(p -> assertTrue(exists(p)));
    }

}
