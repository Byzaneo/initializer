package io.byzaneo.initializer.event;

import io.byzaneo.initializer.bean.Project;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectPostEvent extends ProjectEvent {
    private static final long serialVersionUID = -8321288640164006857L;

    public ProjectPostEvent(Project project) {
        super(project);
    }
}
