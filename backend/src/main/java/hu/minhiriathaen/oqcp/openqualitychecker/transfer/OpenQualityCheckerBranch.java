package hu.minhiriathaen.oqcp.openqualitychecker.transfer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Generated;

@Data
@Generated
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenQualityCheckerBranch {

  private Long id; // NOPMD

  @JsonProperty("branchName")
  private String name;

  @JsonProperty("projectDTO")
  private OpenQualityCheckerProject project;

  @JsonProperty("qualification")
  private OpenQualityCheckerQualificationResult qualificationResult;
}
