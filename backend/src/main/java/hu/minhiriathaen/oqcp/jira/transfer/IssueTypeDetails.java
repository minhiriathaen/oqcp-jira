package hu.minhiriathaen.oqcp.jira.transfer;

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
public class IssueTypeDetails extends IdentifiedJiraObject {

  /** Whether this issue type is used to create subtasks. */
  private boolean subtask;
}
