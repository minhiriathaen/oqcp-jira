package hu.minhiriathaen.oqcp.api.advice.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Generated;

@Data
@Generated
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReasonTransfer {

  private String reason;

  private Double significance;
}
