package hu.minhiriathaen.oqcp.openqualitychecker.transfer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.Generated;

@Data
@Generated
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenQualityCheckerResultWrapper {
  private JsonNode data;
}
