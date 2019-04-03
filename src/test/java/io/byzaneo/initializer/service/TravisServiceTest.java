package io.byzaneo.initializer.service;

import io.byzaneo.initializer.bean.Project;
import io.byzaneo.initializer.service.SourcesService.Context;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.byzaneo.initializer.service.SourcesService.EL;
import static io.byzaneo.initializer.service.SourcesService.EL_CONTEXT;
import static java.nio.file.Files.exists;
import static java.util.Comparator.reverseOrder;
import static org.junit.Assert.assertTrue;

public class TravisServiceTest {

    private Project project;

    @Before
    public void before() throws IOException {
        final Path dir = Paths.get("C:\\Temp\\travis");
        if ( Files.isDirectory(dir) )
            Files.walk(dir)
                    .sorted(reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);

        this.project = Project.builder()
                .name("test")
                .namespace("io.byzaneo")
                .ownerName("Tester")
                .owner("tester@byzaneo.io")
                .directory(dir)
                .build();
    }

    @Test
    public void el() {
        System.out.println(EL.parseExpression("language.name eq 'Java' ?  'jdk: openjdk' + language.version : ''").getValue(EL_CONTEXT, project, String.class));
    }

    @Test
    public void sources() throws IOException {
        new SourcesService().generateSources(new Context(project), "Travis");

        Files.walk(project.getDirectory())
                .peek(System.out::println)
                .forEach(p -> assertTrue(exists(p)));
    }
}
