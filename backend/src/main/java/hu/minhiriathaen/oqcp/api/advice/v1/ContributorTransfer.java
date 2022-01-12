package hu.minhiriathaen.oqcp.api.advice.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Generated;

@Data
@Generated
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContributorTransfer {

  private final String name;

  private final Number numberOfContributions;
}
