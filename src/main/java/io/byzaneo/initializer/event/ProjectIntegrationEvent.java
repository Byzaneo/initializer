package io.byzaneo.initializer.event;

import io.byzaneo.initializer.bean.Project;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectIntegrationEvent extends ProjectEvent {
    private static final long serialVersionUID = 1478305394105817966L;

    public ProjectIntegrationEvent(Project project) {
        super(project);
    }
}
