package hu.minhiriathaen.oqcp.jira.transfer;

import lombok.Data;
import lombok.Generated;

@Data
@Generated
public class CreateIssueTypeRequest {

  /** The unique name for the issue type. The maximum length is 60 characters. */
  private String name;

  /** The description of the issue type. */
  private String description;

  /**
   * Whether the issue type is subtype or standard. Defaults to standard.
   *
   * <p>Valid values: subtask, standard
   */
  private IssueTypeCategory type;
}
