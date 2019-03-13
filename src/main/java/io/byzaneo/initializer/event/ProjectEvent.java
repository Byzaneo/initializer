package io.byzaneo.initializer.event;

import io.byzaneo.initializer.bean.Project;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class ProjectEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1547362734916504388L;

    protected final String name;

    ProjectEvent(Project project) {
        super(project);
        this.name = this.getClass().getSimpleName()
                .replace("Project", "")
                .replace("Event", "")
                .toLowerCase();
    }

    public String getMode() {
        return this.getProject().getMode().toString();
    }

    public Project getProject() {
        return (Project) this.source;
    }

    public Instant getDate() {
        return Instant.ofEpochMilli(this.getTimestamp());
    }
}
