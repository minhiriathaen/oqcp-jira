package hu.minhiriathaen.oqcp.api.advice.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;
import lombok.Generated;

@Data
@Generated
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdviceDetailsTransfer {

  private String id; // NOPMD

  private String advice;

  private ContributorWrapper contributors;

  private List<ReasonTransfer> reasons;

  private Assessment assessment;

  public List<ContributorTransfer> getContributors() {

    return contributors == null ? null : contributors.getContributors();
  }
}
