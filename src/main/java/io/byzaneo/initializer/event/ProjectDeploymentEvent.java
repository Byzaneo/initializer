package io.byzaneo.initializer.event;

import io.byzaneo.initializer.bean.Project;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectDeploymentEvent extends ProjectEvent {

    private static final long serialVersionUID = 2902202837231139018L;

    public ProjectDeploymentEvent(Project project) {
        super(project);
    }
}

