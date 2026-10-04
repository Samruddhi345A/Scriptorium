package com.scriptorium.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeDTO {

    private String id;
    private String title;
    private String language;
    private String approximateDate;
    private String summary;
    private String condition;
    private String originRegion;
    private String holdingInstitution;
    private String shelfmark;
    private String author;
    private String script;
}
