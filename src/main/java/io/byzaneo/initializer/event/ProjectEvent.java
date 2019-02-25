package io.byzaneo.initializer.event;

import io.byzaneo.initializer.bean.Project;
import lombok.Data;

@Data
public abstract class ProjectEvent {
    protected final Project project;
    protected final long timestamp;
    protected final String name;

    ProjectEvent(String name, Project project) {
        this.name = name;
        this.project = project;
        this.timestamp = System.currentTimeMillis();
    }

}
