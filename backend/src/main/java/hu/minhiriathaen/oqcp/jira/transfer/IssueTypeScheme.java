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
public class IssueTypeScheme extends IdentifiedJiraObject {

  /** Whether the issue type scheme is the default.. */
  private Boolean isDefault;

  /** The description of the issue type scheme. */
  private String description;

  /** The ID of the default issue type of the issue type scheme. */
  private String defaultIssueTypeId;
}
