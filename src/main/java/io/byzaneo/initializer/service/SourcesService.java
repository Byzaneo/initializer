package io.byzaneo.initializer.service;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Mustache.Lambda;
import io.byzaneo.initializer.bean.Project;
import io.byzaneo.initializer.event.ProjectSourcesEvent;
import io.byzaneo.initializer.facet.Facet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import static java.net.URLDecoder.decode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.newBufferedWriter;
import static java.util.Arrays.stream;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;
import static org.springframework.util.StringUtils.capitalize;

@Slf4j
@Service
public class SourcesService {

    private static final Mustache.Compiler MUSTACHE = Mustache.compiler();
    private static final PathMatchingResourcePatternResolver RESOLVER = new PathMatchingResourcePatternResolver();

    /* -- EVENTS -- */

    @EventListener(condition = "#event.project.mode == T(io.byzaneo.initializer.Constants$Mode).create")
    @Order(HIGHEST_PRECEDENCE + 10)
    public void onCreateSources(ProjectSourcesEvent event) {
        generateSources(event.getProject());
    }

    /* -- PRIVATE -- */

    private void generateSources(Project project) {
        final Context context = new Context(project);
        project.facets()
                .filter(f -> f.getTemplatesLocation()!=null)
                .forEach(facet -> generateSources(context, facet));
    }

    private void generateSources(Context context, Facet facet) {
        final Path directory = context.project.getDirectory();
        final String templateLocation = "classpath:/templates/"+ facet.getTemplatesLocation()+"/";
        final String root = Optional.of(RESOLVER.getResource(templateLocation))
                    .filter(Resource::exists)
                    .map(this::resourcePath)
                    .orElseThrow(() -> new RuntimeException("Source templates not found at "+templateLocation));
        try (final Stream<Resource> templates =
                     stream(RESOLVER.getResources(templateLocation + "**/*"))) {
            templates
                    .filter(Resource::isReadable) // files only
                    .forEach(tpl -> {
                        final Path dest = directory.resolve(MUSTACHE
                                .compile(resourcePath(tpl)
                                        .replace(root, "")
                                        .replace("{{[", "{{/"))
                                .execute(context));
                        transform(context, tpl, createParentDirectories(dest));
                    });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path createParentDirectories(Path path) {
        try {
            createDirectories(path.getParent());
            return path;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String resourcePath(Resource resource) {
        try {
            return decode(resource.getURL().toString(), UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void transform(Object context, Resource template, Path destination) {
        try (final Reader reader = new BufferedReader(new InputStreamReader(template.getInputStream(), UTF_8));
             final Writer writer = newBufferedWriter(destination, UTF_8)) {
            MUSTACHE.compile(reader).execute(context, writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private final class Context {
        final Project project;
        final String capitalizedName;
        final Lambda folder = (fragment, writer) -> writer.write(fragment.execute().replace('.', '/'));

        private Context(Project project) {
            this.project = project;
            this.capitalizedName = capitalize(project.getName());
        }
    }
}
