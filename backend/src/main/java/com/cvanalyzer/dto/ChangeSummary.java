package com.cvanalyzer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/** Machine-readable summary of what the optimizer changed (comparison view). */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChangeSummary {
    private List<String> reorderedSections = new ArrayList<>();
    private List<String> rewrittenSections = new ArrayList<>();
    private List<String> keywordsEmphasized = new ArrayList<>();
    private List<String> atsImprovements = new ArrayList<>();
}
