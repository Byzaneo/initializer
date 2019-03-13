package io.byzaneo.initializer.event;

import io.byzaneo.initializer.bean.Project;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectSourcesEvent extends ProjectEvent {
    private static final long serialVersionUID = -7041349336931184003L;

    public ProjectSourcesEvent(Project project) {
        super(project);
    }
}
