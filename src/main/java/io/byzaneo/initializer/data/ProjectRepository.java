package io.byzaneo.initializer.data;

import io.byzaneo.initializer.bean.Project;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ProjectRepository extends ReactiveMongoRepository<Project, String> {
    Mono<Project> findByNameAndOwner(String name, String owner);
}
