package hu.minhiriathaen.oqcp.jira.transfer.issue;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import hu.minhiriathaen.oqcp.jira.transfer.IdentifiedJiraObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Generated;
import lombok.ToString;

@Data
@Generated
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class IssueTransition extends IdentifiedJiraObject {

  @JsonProperty("to")
  private IdentifiedJiraObject target;
}
