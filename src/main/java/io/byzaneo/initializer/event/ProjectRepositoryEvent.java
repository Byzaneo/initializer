package io.byzaneo.initializer.event;

import io.byzaneo.initializer.bean.Project;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectRepositoryEvent extends ProjectEvent {
    private static final long serialVersionUID = 2338789298125191494L;

    public ProjectRepositoryEvent(Project project) {
        super(project);
    }
}
