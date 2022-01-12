package hu.minhiriathaen.oqcp.openqualitychecker.transfer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Generated;

@Data
@Generated
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenQualityCheckerProject {

  private Long id; // NOPMD

  @JsonProperty("projectName")
  private String name;
}
