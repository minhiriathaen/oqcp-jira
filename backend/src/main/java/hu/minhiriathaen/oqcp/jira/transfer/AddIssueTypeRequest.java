package hu.minhiriathaen.oqcp.jira.transfer;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.Generated;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Generated
public class AddIssueTypeRequest {

  private final List<String> issueTypeIds = new ArrayList<>();

  @JsonAnySetter
  public void add(final String issueTypeId) {
    issueTypeIds.add(issueTypeId);
  }
}
