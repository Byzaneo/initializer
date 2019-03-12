package io.byzaneo.initializer.event;

import io.byzaneo.initializer.bean.Project;
import lombok.Data;

import java.time.Instant;

import static java.time.Instant.now;

@Data
public abstract class ProjectEvent {
    protected final Project project;
    protected final Instant date;
    protected final String name;

    ProjectEvent(Project project) {
        this.name = this.getClass().getSimpleName()
                .replace("Project", "")
                .replace("Event", "")
                .toLowerCase();
        this.project = project;
        this.date = now();
    }

    public String getMode() {
        return this.project.getMode().toString();
    }
}
