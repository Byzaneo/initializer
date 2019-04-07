package io.byzaneo.initializer;

import java.time.Duration;

import static java.time.Duration.ofSeconds;

public class Constants {

    public static final Duration TIMEOUT = ofSeconds(15);

    public enum FacetFamily {
        Language,
        Management,
        Registry,
        Integration,
        Coverage,
        Quality,
        Deployment,
        Front,
        Repository
    }

    public enum Mode {
        create, update, delete;
    }
}
