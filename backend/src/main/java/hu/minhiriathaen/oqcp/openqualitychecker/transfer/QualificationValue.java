package hu.minhiriathaen.oqcp.openqualitychecker.transfer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Generated;

@Data
@Generated
@JsonIgnoreProperties(ignoreUnknown = true)
public class QualificationValue {

  private String name;

  private Double value;
}
