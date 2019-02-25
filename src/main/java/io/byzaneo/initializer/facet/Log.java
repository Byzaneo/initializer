package io.byzaneo.initializer.facet;

import io.byzaneo.initializer.event.ProjectInitializationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

@Slf4j
@Component
public class Log {

    @EventListener(ProjectInitializationEvent.class)
    public void listen(@NotNull ProjectInitializationEvent event) {
        log.info("{}: {}", event.getName(), event.getProject());
    }
}
