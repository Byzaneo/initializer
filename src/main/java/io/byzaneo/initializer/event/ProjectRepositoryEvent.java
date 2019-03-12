package io.byzaneo.initializer.event;

import io.byzaneo.initializer.bean.Project;
import lombok.Data;

@Data
public class ProjectRepositoryEvent extends ProjectEvent {

    public ProjectRepositoryEvent(Project project) {
        super(project);
    }
}
