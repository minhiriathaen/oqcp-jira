package hu.minhiriathaen.oqcp.api.advice.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Generated;
import lombok.ToString;

@Data
@Generated
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdviceResolvedTransfer extends AdviceTransfer {

  private String id; // NOPMD

  private String commitUrl;
}
