package com.singularbank.mifid.client.pdf.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerationPdfRequest {

  @JsonProperty("templateName")
  private String templateName;

  @JsonProperty("parameters")
  private Map<String, Object> parameters;
}
