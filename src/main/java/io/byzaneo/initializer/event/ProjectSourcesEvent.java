package io.byzaneo.initializer.event;

import io.byzaneo.initializer.bean.Project;
import lombok.Data;

@Data
public class ProjectSourcesEvent extends ProjectEvent {

    public ProjectSourcesEvent(Project project) {
        super(project);
    }
}
