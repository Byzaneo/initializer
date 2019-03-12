package io.byzaneo.initializer.event;

import io.byzaneo.initializer.bean.Project;
import lombok.Data;

@Data
public class ProjectDeploymentEvent extends ProjectEvent {

    public ProjectDeploymentEvent(Project project) {
        super(project);
    }
}
