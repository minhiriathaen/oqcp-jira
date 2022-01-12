package hu.minhiriathaen.oqcp.openqualitychecker.transfer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.Generated;

@Data
@Generated
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenQualityCheckerResultPage {
  private JsonNode content;
  private Boolean empty;
  private Boolean first;
  private Boolean last;

  @JsonProperty("number")
  private Integer pageNumber;

  private Integer size;
  private Integer numberOfElements;
  private Long totalElements;
  private Integer totalPages;
}
