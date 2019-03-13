package io.byzaneo.initializer.event;

import io.byzaneo.initializer.bean.Project;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectPreEvent extends ProjectEvent {
    private static final long serialVersionUID = 7186857881189683460L;

    public ProjectPreEvent(Project project) {
        super(project);
    }
}
