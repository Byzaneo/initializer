package io.byzaneo.initializer.event;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectErrorEvent extends ProjectEvent {
    private static final long serialVersionUID = -8703225164260645635L;

    private final Throwable error;
    private final ProjectEvent origin;
    public ProjectErrorEvent(ProjectEvent origin, Throwable error) {
        super(origin.getProject());
        this.origin = origin;
        this.error = error;
    }
}
