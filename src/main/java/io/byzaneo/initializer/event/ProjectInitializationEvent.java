package io.byzaneo.initializer.event;

import io.byzaneo.initializer.bean.Project;
import lombok.Data;

@Data
public class ProjectInitializationEvent extends ProjectEvent {

    public ProjectInitializationEvent(Project project) {
        super("Initialization", project);
    }
}
