package io.byzaneo.initializer.event;

import io.byzaneo.initializer.bean.Project;
import lombok.Data;

@Data
public class ProjectPersistencyEvent extends ProjectEvent {

    public ProjectPersistencyEvent(Project project) {
        super(project);
    }
}
