package io.byzaneo.initializer.data;

import io.byzaneo.initializer.bean.Project;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ProjectRepository extends ReactiveMongoRepository<Project, String> {
}
