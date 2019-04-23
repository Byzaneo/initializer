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
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static io.byzaneo.initializer.service.InitializerService.CONDITION_CREATE;
import static io.byzaneo.initializer.service.InitializerService.CONDITION_UPDATE;
import static java.net.URLDecoder.decode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.newBufferedWriter;
import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@Slf4j
@Service
public class SourcesService {

    private static final Mustache.Compiler MUSTACHE = Mustache.compiler();
    private static final PathMatchingResourcePatternResolver RESOLVER = new PathMatchingResourcePatternResolver();
    static final ExpressionParser EL = new SpelExpressionParser();
    static final EvaluationContext EL_CONTEXT = SimpleEvaluationContext.forReadOnlyDataBinding().build();


    /* -- EVENTS -- */

    @EventListener(condition = CONDITION_CREATE + " or " + CONDITION_UPDATE)
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
        // README
        this.generateSources(context, "Readme");
    }

    private void generateSources(final Context context, final Facet facet) {
        generateSources(context, facet.getTemplatesLocation());
    }

    void generateSources(final Context context, final String templatesLocation) {
        log.info("{}: {} sources generation", context.project.getName(), templatesLocation);
        final Path directory = context.project.getDirectory();
        final String templateLocation = "classpath:/templates/"+ templatesLocation +"/";
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
                        log.debug("- {} > {}", tpl, dest);
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
        } catch (RuntimeException re) {
            log.error("Error transforming template {}", template);
            throw re;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @SuppressWarnings("unused")
    static final class Context {
        final Project project;
        final Lambda folder = (fragment, writer) -> writer.write(fragment.execute().replace('.', '/'));
        final Lambda uppercase = (fragment, writer) -> writer.write(fragment.execute().toUpperCase());
        final Lambda lowercase = (fragment, writer) -> writer.write(fragment.execute().toLowerCase());
        final Lambda capitalize = (fragment, writer) -> writer.write(StringUtils.capitalize(fragment.execute()));
        final Lambda el;
        final String openBracket = "{";
        final Map<String, Boolean> facets;

        Context(Project project) {
            this.project = project;
            this.el = (fragment, writer) -> writer.write(requireNonNull(EL.parseExpression(fragment.execute()).getValue(EL_CONTEXT, project, String.class)));
            this.facets = new HashMap<>();
            this.project.facets()
                    .forEach(f -> this.facets.put(f.getId(), true));
        }
    }
}
