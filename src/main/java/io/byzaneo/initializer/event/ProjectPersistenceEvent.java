package io.byzaneo.initializer.event;

import io.byzaneo.initializer.bean.Project;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectPersistenceEvent extends ProjectEvent {
    private static final long serialVersionUID = 347944989642241480L;

    public ProjectPersistenceEvent(Project project) {
        super(project);
    }
}
