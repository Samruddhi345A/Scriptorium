package com.scriptorium.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RelationshipRequest {

    @NotBlank(message = "Source manuscript ID is required")
    private String sourceId;

    @NotBlank(message = "Target manuscript ID is required")
    private String targetId;

    @NotBlank(message = "Relationship type is required")
    @Pattern(
        regexp = "TRANSLATED_FROM|COPIED_FROM|COMMENTARY_ON|DERIVED_FROM|PARALLEL_TRADITION|INFLUENCED_BY",
        message = "Type must be one of: TRANSLATED_FROM, COPIED_FROM, COMMENTARY_ON, DERIVED_FROM, PARALLEL_TRADITION, INFLUENCED_BY"
    )
    private String type;

    @NotBlank(message = "Confidence is required")
    @Pattern(
        regexp = "established|probable|speculative",
        message = "Confidence must be: established, probable, or speculative"
    )
    private String confidence;

    private String description;
    private String scholarlySource;
}
