package io.byzaneo.initializer.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import javax.validation.constraints.NotNull;

import static org.apache.commons.lang.exception.ExceptionUtils.getRootCauseMessage;

@Getter
public class ErrorEvent extends ApplicationEvent {
    private static final long serialVersionUID = -8703225164260645635L;

    private final Throwable error;

    public ErrorEvent(@NotNull ProjectEvent source, @NotNull Throwable error) {
        super(source);
        this.error = error;
    }

    @Override
    public String toString() {
        return this.source.getClass().getSimpleName()+": "+getRootCauseMessage(this.error);
    }
}
