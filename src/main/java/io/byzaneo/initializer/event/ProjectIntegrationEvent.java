package io.byzaneo.initializer.event;

import io.byzaneo.initializer.bean.Project;
import lombok.Data;

@Data
public class ProjectIntegrationEvent extends ProjectEvent {

    public ProjectIntegrationEvent(Project project) {
        super(project);
    }
}
