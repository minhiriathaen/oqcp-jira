package hu.minhiriathaen.oqcp.jira.transfer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Generated;

@Data
@Generated
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaginatedResponse {

  private Integer maxResults;

  private Integer startAt; // 0 based

  private Integer total;

  private Boolean isLast;
}
