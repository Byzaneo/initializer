package io.byzaneo.initializer.rest;

import io.byzaneo.initializer.bean.Project;
import io.byzaneo.initializer.service.InitializerService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final InitializerService initalizerService;

    public ProjectController(InitializerService sourceService) {
        this.initalizerService = sourceService;
    }


    @PostMapping
    public Mono<Project> create(@RequestBody @Valid Project project) {
        return this.initalizerService.create(project);
    }
}
