package io.byzaneo.initializer;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import io.byzaneo.initializer.bean.Project;
import org.junit.Test;

import java.io.File;
import java.util.Map;

import static org.junit.Assert.assertEquals;

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
    public void projectContext() {
        String text = "(#folder)(project.group)(/folder)";
        Template tmpl = Mustache.compiler().withDelims("( )").compile(text);
        final Object context = new Object() {
            Project project = Project.builder()
                    .name("test")
                    .owner("tester@byzaneo.io")
                    .organization("byzaneo")
                    .build();
            Mustache.Lambda folder = (fragment, writer) -> writer.write(fragment.execute().replace('.', File.separatorChar));
        };

        System.out.println(tmpl.execute(context));
        assertEquals(
                "io"+File.separatorChar+"byzaneo",
                tmpl.execute(context));
    }
}
