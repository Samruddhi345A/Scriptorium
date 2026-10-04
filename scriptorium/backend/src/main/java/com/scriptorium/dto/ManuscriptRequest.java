package com.scriptorium.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ManuscriptRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String originalTitle;

    @NotBlank(message = "Language code is required")
    @Pattern(regexp = "^[a-z]{2,3}$", message = "Language must be a valid ISO 639 code (2-3 lowercase letters)")
    private String language;

    private String script;
    private String approximateDate;
    private Integer dateLower;
    private Integer dateUpper;
    private String originRegion;
    private String scribe;
    private String author;
    private String holdingInstitution;
    private String shelfmark;
    private String condition;
    private String summary;
    private String contentUrl;
    private Integer wordCount;

    // Optional — cluster to attach to
    private String clusterId;
}
