package io.byzaneo.initializer.service;

import io.byzaneo.initializer.bean.Project;
import io.byzaneo.initializer.event.ProjectSourcesEvent;
import io.byzaneo.initializer.facet.Java;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import static java.util.Optional.ofNullable;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@Slf4j
@Service
public class SourcesService {

    /* -- EVENTS -- */

    @EventListener(condition = "#event.project.mode == T(io.byzaneo.initializer.Constants$Mode).create")
    @Order(HIGHEST_PRECEDENCE + 10)
    public void createSources(ProjectSourcesEvent event) {
        final Project project = event.getProject();
        Assert.isTrue(Java.FACET_NAME.equals(project.getLanguage().getName()), "Only Java language is supported");
        final Java java = project.getLanguage();
        final String namespace = ofNullable(java.getNamespace())
                    .orElse("io.byzaneo."+ project.getName());

        log.info("Creating Java {} sources: {}", java.getVersion(), namespace);
    }

}
