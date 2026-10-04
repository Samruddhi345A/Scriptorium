package com.scriptorium.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EdgeDTO {

    private String id;
    private String source;
    private String target;
    private String type;          // TRANSLATED_FROM, COPIED_FROM, etc.
    private String confidence;    // established, probable, speculative
    private String description;
    private String scholarlySource;
}
