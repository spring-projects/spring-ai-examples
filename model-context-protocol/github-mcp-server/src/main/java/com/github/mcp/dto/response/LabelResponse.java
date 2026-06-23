package com.github.mcp.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LabelResponse {
    private Long id;
    private String nodeId;
    private String url;
    private String name;
    private String description;
    private String color;
    private Boolean default_label;
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public void setDefault(Boolean default_label) {
        this.default_label = default_label;
    }
}
