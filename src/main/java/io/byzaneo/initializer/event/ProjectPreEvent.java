package io.byzaneo.initializer.event;

import io.byzaneo.initializer.bean.Project;
import lombok.Data;

@Data
public class ProjectPreEvent extends ProjectEvent {

    public ProjectPreEvent(Project project) {
        super(project);
    }
}
