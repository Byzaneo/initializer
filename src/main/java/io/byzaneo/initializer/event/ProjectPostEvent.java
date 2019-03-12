package io.byzaneo.initializer.event;

import io.byzaneo.initializer.bean.Project;
import lombok.Data;

@Data
public class ProjectPostEvent extends ProjectEvent {

    public ProjectPostEvent(Project project) {
        super(project);
    }
}
