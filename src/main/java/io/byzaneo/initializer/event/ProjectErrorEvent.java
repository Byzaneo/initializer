package io.byzaneo.initializer.event;

import io.byzaneo.initializer.bean.Project;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import javax.validation.constraints.NotNull;

import static org.apache.commons.lang.exception.ExceptionUtils.getRootCauseMessage;

@Getter
public class ProjectErrorEvent extends ApplicationEvent {
    private static final long serialVersionUID = -8703225164260645635L;

    private final Throwable error;

    public ProjectErrorEvent(@NotNull ProjectEvent source, @NotNull Throwable error) {
        super(source);
        this.error = error;
    }

    public Project getProject() {
        return ((ProjectEvent) this.source).getProject();
    }

    @Override
    public String toString() {
        return this.source.getClass().getSimpleName()+": "+getRootCauseMessage(this.error);
    }
}
