package io.byzaneo.initializer.bean;

import io.byzaneo.initializer.Constants;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = Facet.COLLECTION)
public class Facet {

    public static final String COLLECTION = "facets";

    @Id
    @NonNull
    @NotBlank
    private String id;
    @NonNull
    @NotBlank
    private Constants.Facet type;
    @NonNull
    @NotBlank
    private String name;
    private String group;
    private String home;

    // - API -
    private String api;
    private String token;
//
//    // - host -
//    private String host;
    private String username;
    private String password;
}
